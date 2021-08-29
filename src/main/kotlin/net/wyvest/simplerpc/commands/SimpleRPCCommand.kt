package net.wyvest.simplerpc.commands

import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.SubCommand
import net.wyvest.simplerpc.SimpleRPC
import net.wyvest.simplerpc.config.RPCConfig

@Suppress("unused")
object SimpleRPCCommand : Command(SimpleRPC.ID, true) {

    override val commandAliases = setOf(
        Alias("rpc")
    )

    @DefaultHandler
    fun handle() {
        EssentialAPI.getGuiUtil().openScreen(RPCConfig.gui())
    }

    @SubCommand("config", description = "Opens the config GUI for " + SimpleRPC.NAME)
    fun config() {
        EssentialAPI.getGuiUtil().openScreen(RPCConfig.gui())
    }
}