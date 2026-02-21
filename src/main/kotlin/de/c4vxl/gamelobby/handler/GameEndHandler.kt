package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.lobby.Lobby
import de.c4vxl.gamemanager.gma.event.game.GameEndEvent
import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import net.kyori.adventure.title.TitlePart
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Handles game endings
 * Displays winners and moves players back to lobby
 */
class GameEndHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onGameEnd(event: GameEndEvent) {
        if (event.winnerTeam == null)
            return

        // Display global win message
        event.game.players.forEach {
            it.bukkitPlayer.sendTitlePart(TitlePart.TITLE, it.language
                .child("gamelobby")
                .getCmp("end.winner.title", event.winnerTeam?.label ?: "/")
            )
        }

        // Display lost message
        event.teamsLost.forEach { it.players.forEach { player ->
            player.bukkitPlayer.sendTitlePart(TitlePart.TITLE, player.language
                .child("gamelobby")
                .getCmp("end.lost.title", event.winnerTeam?.label ?: "/")
            )
        } }

        // Display winner team win message
        event.game.players.forEach {
            it.bukkitPlayer.sendActionBar(it.language
                .child("gamelobby")
                .getCmp("end.winner.title", event.winnerTeam?.label ?: "/")
            )
        }

        // Display won message
        event.winnerTeam?.players?.forEach {
            it.bukkitPlayer.sendTitlePart(TitlePart.TITLE, it.language.child("gamelobby").getCmp("end.won.title"))
        }
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        event.kickPlayers = false

        // Move players to lobby
        event.game.worldManager.map?.world?.players?.forEach {
            Lobby.send(it)
        }

        event.game.worldManager.map?.unload()
    }
}