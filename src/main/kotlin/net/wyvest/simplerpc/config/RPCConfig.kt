package net.wyvest.simplerpc.config

import gg.essential.api.EssentialAPI
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import net.wyvest.simplerpc.SimpleRPC
import net.wyvest.simplerpc.SimpleRPC.NAME
import net.wyvest.simplerpc.SimpleRPC.mc
import net.wyvest.simplerpc.gui.DownloadConfirmGui
import net.wyvest.simplerpc.utils.Updater
import java.io.File
import kotlin.properties.Delegates

@Suppress("unused")
object RPCConfig : Vigilant(File(SimpleRPC.modDir, "${SimpleRPC.ID}.toml"), NAME) {

    var lastToggled by Delegates.notNull<Boolean>()

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Mod",
        description = "Toggle the mod.\nRequires a restart to take effect.",
        category = "General"
    )
    var toggled = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Automatically Turn Off Mod When HyCord Detected",
        description = "Automatically turn off SimpleRPC when HyCord is detected.",
        category = "General"
    )
    var hycordDetect = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Retain SimpleRPC RPC Text With HyCord",
        description = "Retain the same state, details, and time elapsed of the RPC in SimpleRPC when using HyCord.",
        category = "General"
    )
    var keep = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Details",
        description = "Show the details of the RPC.",
        category = "General"
    )
    var showDetails = true

    @Property(
        type = PropertyType.SELECTOR,
        name = "Select Details",
        description = "Select the type of detail for the RPC.",
        category = "General",
        options = ["Current Server", "Current User", "Current Item Held", "Current Amount of Players"]
    )
    var details = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Image",
        description = "Show the image for the RPC.",
        category = "General"
    )
    var showImage = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Time Elapsed",
        description = "Show the time elapsed while playing Minecraft.",
        category = "General"
    )
    var showTime = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Update Notification",
        description = "Show a notification when you start Minecraft informing you of new updates.",
        category = "Updater"
    )
    var showUpdateNotification = true

    @Property(
        type = PropertyType.BUTTON,
        name = "Update Now",
        description = "Update $NAME by clicking the button.",
        category = "Updater"
    )
    fun update() {
        if (Updater.shouldUpdate) EssentialAPI.getGuiUtil()
            .openScreen(DownloadConfirmGui(mc.currentScreen)) else EssentialAPI.getNotifications()
            .push(NAME, "No update had been detected at startup, and thus the update GUI has not been shown.")
    }

    init {
        initialize()
        registerListener("toggled") {
                toggled: Boolean ->
            run {
                if (toggled != lastToggled) {
                    EssentialAPI.getNotifications().push("SimpleRPC", "Successfully toggled. Please restart your game for it to take effect.")
                }
            }
        }
    }
}