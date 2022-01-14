package net.wyvest.simplerpc.updater

import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import gg.essential.api.utils.WebUtil.downloadToFile
import gg.essential.api.utils.WebUtil.fetchJSON
import gg.essential.universal.UDesktop.open
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import net.wyvest.simplerpc.SimpleRPC
import net.wyvest.simplerpc.config.RPCConfig
import java.io.File
import java.io.IOException

object Updater {
    var updateUrl = ""
    var latestTag: String? = null
    var shouldUpdate = false

    fun update() {
        Multithreading.runAsync {
            try {
                val latestRelease =
                    fetchJSON("https://api.github.com/repos/W-OVERFLOW/${SimpleRPC.ID}/releases/latest").getObject()
                latestTag = latestRelease["tag_name"].asString
                val currentVersion =
                    DefaultArtifactVersion(SimpleRPC.VERSION.substringBefore("-"))
                val latestVersion = DefaultArtifactVersion(latestTag!!.substringAfter("v").substringBefore("-"))
                if (currentVersion >= latestVersion) {
                    return@runAsync
                }
                updateUrl =
                    latestRelease["assets"].asJsonArray[0].asJsonObject["browser_download_url"]
                        .asString
                if (updateUrl.isNotEmpty()) {
                    if (RPCConfig.showUpdateNotification) {
                        EssentialAPI.getNotifications().push(
                            SimpleRPC.NAME,
                            "${SimpleRPC.NAME} has a new update ($latestTag)! Click here to download it automatically!", action = { EssentialAPI.getGuiUtil().openScreen(DownloadGui()) }
                        )
                    }
                    shouldUpdate = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun download(url: String, file: File): Boolean {
        var url = url
        if (file.exists()) return true
        url = url.replace(" ", "%20")
        try {
            downloadToFile(url, file, "${SimpleRPC.NAME}/${SimpleRPC.VERSION}")
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return file.exists()
    }

    /**
     * Adapted from RequisiteLaunchwrapper under LGPLv3
     * https://github.com/Qalcyo/RequisiteLaunchwrapper/blob/main/LICENSE
     */
    fun addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Opening Deleter task...")
            try {
                val runtime = javaRuntime
                if (Minecraft.isRunningOnMac) {
                    open(SimpleRPC.jarFile.parentFile)
                }
                val file = File(SimpleRPC.modDir.parentFile, "Deleter-1.2.jar")
                Runtime.getRuntime()
                    .exec("\"" + runtime + "\" -jar \"" + file.absolutePath + "\" \"" + SimpleRPC.jarFile.absolutePath + "\"")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Thread.currentThread().interrupt()
        })
    }

    /**
     * Gets the current Java runtime being used.
     *
     * @link https://stackoverflow.com/a/47925649
     */
    @get:Throws(IOException::class)
    val javaRuntime: String
        get() {
            val os = System.getProperty("os.name")
            val java = System.getProperty("java.home") + File.separator + "bin" + File.separator +
                    if (os != null && os.lowercase().startsWith("windows")) "java.exe" else "java"
            if (!File(java).isFile) {
                throw IOException("Unable to find suitable java runtime at $java")
            }
            return java
        }
}