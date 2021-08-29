package net.wyvest.simplerpc

import dev.cbyrne.kdiscordipc.DiscordIPC
import dev.cbyrne.kdiscordipc.event.DiscordEvent
import dev.cbyrne.kdiscordipc.listener.IPCListener
import dev.cbyrne.kdiscordipc.presence.presence
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.wyvest.simplerpc.commands.SimpleRPCCommand
import net.wyvest.simplerpc.config.RPCConfig
import net.wyvest.simplerpc.config.SimpleConfig
import net.wyvest.simplerpc.utils.Updater
import xyz.matthewtgm.requisite.util.ChatHelper
import java.awt.event.ActionListener
import java.io.File
import javax.swing.Timer
import kotlin.math.floor


@Mod(name = SimpleRPC.NAME, modid = SimpleRPC.ID, version = SimpleRPC.VERSION, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object SimpleRPC {
    const val NAME = "SimpleRPC"
    const val VERSION = "0.2.0"
    const val ID = "simplerpc"
    val mc: Minecraft
        get() = Minecraft.getMinecraft()
    fun sendMessage(message: String?) {
        ChatHelper.sendMessage(EnumChatFormatting.DARK_PURPLE.toString() + "[$NAME] ", message)
    }
    lateinit var jarFile: File
    val modDir = File(File(File(mc.mcDataDir, "config"), "Wyvest"), NAME)
    private val ipc = DiscordIPC("862536466793103411")
    private var secondsPassed = 0
    private var timerTask = ActionListener {
        secondsPassed += 1
        ipc.presence = presence {
            state = "Playing Minecraft 1.8.9"
            details = calculateTime()
            if (SimpleConfig.showImage) {
                largeImageKey = "grass_side"
                largeImageText = "Powered by SimpleRPC!"
            } else {
                largeImageKey = null
                largeImageText = null
            }
        }
    }
    private var timer = Timer(1000, timerTask)

    @Mod.EventHandler
    private fun onFMLPreInitialization(event: FMLPreInitializationEvent) {
        if (!modDir.exists()) modDir.mkdirs()
        jarFile = event.sourceFile
    }

    @Mod.EventHandler
    fun onFMLInitialization(event: FMLInitializationEvent) {
        SimpleConfig.initialize()
        //AdvancedConfig.initialize()
        RPCConfig.initialize()
        SimpleRPCCommand.register()
        Updater.update()
        Multithreading.runAsync {
            if (RPCConfig.toggled) {
                ipc.presence = presence {
                    state = "Playing Minecraft 1.8.9"
                    details = "00:00:00 elapsed."
                    if (SimpleConfig.showImage) {
                        largeImageKey = "grass_side"
                        largeImageText = "Powered by SimpleRPC!"
                    } else {
                        largeImageKey = null
                        largeImageText = null
                    }
                }

                ipc.listener = object : IPCListener {
                    override fun onReadyEvent(event: DiscordEvent.Ready) {
                        println("${event.user} connecting...")
                    }
                }

                ipc.connect()
                timer.start()
            }
        }
        EssentialAPI.getShutdownHookUtil().register {
            //we don't need to disconnect because somehow the IPC already disconnects
            println("Shutting down timer task...")
            try {
                timer.stop()
            } catch (e : Exception) {
                println("The timer failed to shut down, this is likely due to the timer already being stopped.")
            }
        }
    }

    private fun calculateTime(): String {
        val hours = floor((secondsPassed / 3600).toFloat()).toInt()
        val minutes = floor(((secondsPassed - hours * 3600) / 60).toFloat()).toInt()
        val seconds = secondsPassed - hours * 3600 - minutes * 60
        return "${String.format("%02d", hours)}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)} elapsed."
    }
}