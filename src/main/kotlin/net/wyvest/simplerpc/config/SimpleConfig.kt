package net.wyvest.simplerpc.config

import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import net.wyvest.simplerpc.SimpleRPC
import java.io.File

object SimpleConfig : Vigilant(File(SimpleRPC.modDir, "${SimpleRPC.ID}-simple.toml"), "Simple") {

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Image",
        description = "Show the image for the RPC.",
        category = "General"
    )
    var showImage = true

}