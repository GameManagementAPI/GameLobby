package de.c4vxl.gamelobby.handler

import de.c4vxl.gamemanager.gamemanagementapi.event.GameFinishEvent
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class GameFinishHandler(plugin: Plugin) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onGameFinish(event: GameFinishEvent) {
        val world: World = event.game.worldManager.world ?: return
        val component = Component.text("Team ")
            .append(LegacyComponentSerializer.legacySection().deserialize(event.winnerTeam.name))
            .append(Component.text(" WON").color(NamedTextColor.GREEN))
            .append(Component.text(" the game!"))


        world.players.forEach {
            if (it.asGamePlayer.team != event.winnerTeam) it.sendTitlePart(TitlePart.TITLE, component)
            it.sendMessage(component)
        }

        event.winnerTeam.players.forEach {
            it.bukkitPlayer.sendTitlePart(TitlePart.TITLE, Component.text("Your team")
                .append(Component.text(" WON").color(NamedTextColor.GREEN))
                .append(Component.text(" the game!")))
        }
    }
}