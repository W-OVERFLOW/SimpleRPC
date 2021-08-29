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

object RPCConfig : Vigilant(File(SimpleRPC.modDir, "${SimpleRPC.ID}.toml"), NAME) {

    @Property(
        type = PropertyType.SWITCH,
        name = "Toggle Mod",
        description = "Toggle the mod.\nRequires a restart of Minecraft.",
        category = "General"
    )
    var toggled = true

    @Property(
        type = PropertyType.SELECTOR,
        name = "RPC Mode",
        description = "Choose the mode of the RPC.",
        category = "General",
        options = ["Simple", "Advanced"]
    )
    var mode = 0

    @Property(
        type = PropertyType.BUTTON,
        name = "Access Options",
        description = "Click the button to access the settings.",
        category = "General"
    )
    fun access() {
        if (mode == 0) {
            EssentialAPI.getGuiUtil().openScreen(SimpleConfig.gui())
        } else {
            EssentialAPI.getNotifications().push("SimpleRPC", "Advanced has not been implemented yet :(")
        }
    }

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
}