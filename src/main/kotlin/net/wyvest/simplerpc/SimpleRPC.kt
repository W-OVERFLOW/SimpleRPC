package net.wyvest.simplerpc

import dev.cbyrne.kdiscordipc.DiscordIPC
import dev.cbyrne.kdiscordipc.presence.presence
import gg.essential.api.EssentialAPI
import io.github.dediamondpro.hycord.features.discord.RichPresence
import io.github.dediamondpro.hycord.options.Settings
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.wyvest.simplerpc.commands.SimpleRPCCommand
import net.wyvest.simplerpc.config.RPCConfig
import net.wyvest.simplerpc.updater.Updater
import java.awt.event.ActionListener
import java.io.File
import java.time.Instant
import javax.swing.Timer


@Mod(
    name = SimpleRPC.NAME,
    modid = SimpleRPC.ID,
    version = SimpleRPC.VERSION,
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
)
object SimpleRPC {
    private var disconnectedHypixel = false
    const val NAME = "SimpleRPC"
    const val VERSION = "1.3.0"
    const val ID = "simplerpc"
    val mc: Minecraft
        get() = Minecraft.getMinecraft()

    lateinit var jarFile: File
    val modDir = File(File(mc.mcDataDir, "W-OVERFLOW"), NAME)
    private val regex = Regex("§[a-z0-9]")
    val ipc = DiscordIPC("862536466793103411")
    private var startTime = Instant.now().epochSecond
    private var timerTask = ActionListener {
        ipc.presence = presence {
            state = "Playing Minecraft 1.8.9"
            if (RPCConfig.showDetails) {
                details = when (RPCConfig.details) {
                    0 -> if (mc.currentServerData == null) {
                        null
                    } else {
                        "Playing on ${mc.currentServerData.serverIP}"
                    }
                    1 -> if (mc.thePlayer == null) {
                        null
                    } else {
                        "Playing as ${mc.thePlayer.name}"
                    }
                    2 -> "Holding ${getCurrentItem()}"
                    3 -> if (mc.netHandler == null) {
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
            }
            if (RPCConfig.showImage) {
                largeImageKey = "grass_side"
                largeImageText = "Powered by SimpleRPC by W-OVERFLOW"
            }
            if (RPCConfig.showTime) {
                startTimestamp = startTime
            }
        }
    }
    private var timer = Timer(1000, timerTask)
    private var hycordDetected = false
    private var sccDetected = false
    private val canConnect
        get() = if (RPCConfig.sccDetect) {
        !sccDetected} else {true}

    @Mod.EventHandler
    private fun onFMLPreInitialization(event: FMLPreInitializationEvent) {
        if (!modDir.exists()) modDir.mkdirs()
        jarFile = event.sourceFile
    }

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        RPCConfig.preload()
        RPCConfig.lastToggled = RPCConfig.toggled
        SimpleRPCCommand.register()
        Updater.update()
        EVENT_BUS.register(this)
        sccDetected = Loader.isModLoaded("skyclientcosmetics")
        hycordDetected = Loader.isModLoaded("hycord")
        if (RPCConfig.toggled) {
            ipc.presence = presence {
                state = "Playing Minecraft 1.8.9"
                if (RPCConfig.showImage) {
                    largeImageKey = "grass_side"
                    largeImageText = "Powered by SimpleRPC by W-OVERFLOW"
                }
                if (RPCConfig.showTime) {
                    startTimestamp = startTime
                }
            }
            if (canConnect) {
                try {
                    ipc.connect()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            timer.start()
        }
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) {
            if (hycordDetected) {
                if (Settings.enableRP) {
                    if (RichPresence.enabled) {
                        if (!disconnectedHypixel && RPCConfig.hycordDetect) {
                            try {
                                if (canConnect) {
                                    try {
                                        ipc.disconnect()
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                    }
                                }
                                disconnectedHypixel = true
                            } catch (e: Exception) {
                                EssentialAPI.getNotifications().push("SimpleRPC", "There was a problem trying to disable SimpleRPC IPC.")
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onServerLeave(e: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        if (disconnectedHypixel && hycordDetected && RPCConfig.hycordDetect) {
            try {
                if (canConnect) {
                    try {
                        ipc.connect()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
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
}