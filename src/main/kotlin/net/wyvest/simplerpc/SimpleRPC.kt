package net.wyvest.simplerpc

import dev.cbyrne.kdiscordipc.DiscordIPC
import dev.cbyrne.kdiscordipc.presence.presence
import gg.essential.api.EssentialAPI
import io.github.dediamondpro.hycord.options.Settings
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.wyvest.simplerpc.commands.SimpleRPCCommand
import net.wyvest.simplerpc.config.RPCConfig
import net.wyvest.simplerpc.utils.Updater
import xyz.matthewtgm.requisite.util.ChatHelper
import xyz.matthewtgm.requisite.util.ForgeHelper
import java.awt.event.ActionListener
import java.io.File
import javax.swing.Timer
import kotlin.math.floor


@Mod(
    name = SimpleRPC.NAME,
    modid = SimpleRPC.ID,
    version = SimpleRPC.VERSION,
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
)
object SimpleRPC {
    private var disconnectedHypixel = false
    const val NAME = "SimpleRPC"
    const val VERSION = "1.0.1"
    const val ID = "simplerpc"
    val mc: Minecraft
        get() = Minecraft.getMinecraft()

    fun sendMessage(message: String?) {
        ChatHelper.sendMessage(EnumChatFormatting.DARK_PURPLE.toString() + "[$NAME] ", message)
    }

    lateinit var jarFile: File
    val modDir = File(File(File(mc.mcDataDir, "config"), "Wyvest"), NAME)
    private val regex = Regex("§[a-z0-9]")
    val ipc = DiscordIPC("862536466793103411")
    private var secondsPassed = 0
    private var timerTask = ActionListener {
        secondsPassed += 1
        ipc.presence = presence {
            state = "Playing Minecraft 1.8.9"
            details = if (RPCConfig.showDetails) {
                val newDetails = when (RPCConfig.details) {
                    0 -> calculateTime()
                    1 -> if (mc.currentServerData == null) {
                        null
                    } else {
                        "Playing on ${mc.currentServerData.serverIP}"
                    }
                    2 -> if (mc.thePlayer == null) {
                        null
                    } else {
                        "Playing as ${mc.thePlayer.name}"
                    }
                    3 -> "Holding ${getCurrentItem()}"
                    4 -> if (mc.netHandler == null) {
                        null
                    } else {
                        "Fighting ${
                            if (mc.netHandler.playerInfoMap.isEmpty()) {
                                0
                            } else {
                                mc.netHandler.playerInfoMap.size - 1
                            }
                        } other players."
                    }
                    else -> null
                }
                if ((newDetails == null) || (newDetails.length >= 128)) {
                    null
                } else {
                    newDetails
                }
            } else {
                null
            }
            if (RPCConfig.showImage) {
                largeImageKey = "grass_side"
                largeImageText = "Powered by SimpleRPC!"
            } else {
                largeImageKey = null
                largeImageText = null
            }
        }
    }
    private var timer = Timer(1000, timerTask)
    private var hycordDetected = false

    @Mod.EventHandler
    private fun onFMLPreInitialization(event: FMLPreInitializationEvent) {
        if (!modDir.exists()) modDir.mkdirs()
        jarFile = event.sourceFile
    }

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        RPCConfig.preload()
        SimpleRPCCommand.register()
        Updater.update()
        EVENT_BUS.register(this)
        hycordDetected = ForgeHelper.isModLoaded("hycord")
        if (RPCConfig.toggled) {
            ipc.presence = presence {
                state = "Playing Minecraft 1.8.9"
                details = "00:00:00 elapsed."
                if (RPCConfig.showImage) {
                    largeImageKey = "grass_side"
                    largeImageText = "Powered by SimpleRPC!"
                } else {
                    largeImageKey = null
                    largeImageText = null
                }
            }

            ipc.connect()
            timer.start()
        }
        EssentialAPI.getShutdownHookUtil().register {
            //we don't need to disconnect because somehow the IPC already disconnects
            println("Shutting down IPC and timer task...")
            try {
                ipc.disconnect()
            } catch (_: Exception) {
                println("The Discord IPC failed to shut down, this is likely due to the IPC already being stopped.")
            }
            try {
                timer.stop()
            } catch (_: Exception) {
                println("The timer failed to shut down, this is likely due to the timer already being stopped.")
            }
        }
    }

    @SubscribeEvent
    fun onServerJoin(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) {
            if (EssentialAPI.getMinecraftUtil().isHypixel() && hycordDetected) {
                if (Settings.enableRP && !disconnectedHypixel) {
                    try {
                        ipc.disconnect()
                        disconnectedHypixel = true
                    } catch (e: Exception) {
                        EssentialAPI.getNotifications().push("SimpleRPC", "There was a problem trying to disable SimpleRPC IPC.")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onServerLeave(e: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        if (disconnectedHypixel && hycordDetected) {
            try {
                ipc.connect()
                disconnectedHypixel = false
            } catch (e: Exception) {
                EssentialAPI.getNotifications().push("SimpleRPC", "There was a problem trying to enable SimpleRPC IPC.")
            }
        }
    }

    private fun getCurrentItem(): String {
        if (mc.thePlayer != null) {
            if (mc.thePlayer.currentEquippedItem != null) {
                if (mc.thePlayer.currentEquippedItem.displayName.replace(regex, "").trim().isNotEmpty()) {
                    return mc.thePlayer.currentEquippedItem.displayName.replace(regex, "")
                }
            }
        }
        return "nothing"
    }

    private fun calculateTime(): String {
        val hours = floor((secondsPassed / 3600).toFloat()).toInt()
        val minutes = floor(((secondsPassed - hours * 3600) / 60).toFloat()).toInt()
        val seconds = secondsPassed - hours * 3600 - minutes * 60
        return "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${
            String.format(
                "%02d",
                seconds
            )
        } elapsed"
    }
}