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

        val winnerLabel = if (event.game.size.teamSize == 1) event.winnerTeam?.players?.firstOrNull()?.bukkitPlayer?.name ?: "/"
                          else event.winnerTeam?.label ?: "/"

        // Display global win message
        event.game.players.forEach {
            it.bukkitPlayer.sendTitlePart(TitlePart.TITLE, it.language
                .child("gamelobby")
                .getCmp("end.winner.title", winnerLabel)
            )
        }

        // Display lost message
        event.teamsLost.forEach { it.players.filter { p -> p.game == event.game }.forEach { player ->
            player.bukkitPlayer.sendActionBar(player.language
                .child("gamelobby")
                .getCmp(
                    if (event.game.size.teamSize == 1) "end.lost.single.title"
                    else "end.lost.title"
                )
            )
        } }

        // Display won message
        event.winnerTeam?.players?.filter { p -> p.game == event.game }?.forEach {
            it.bukkitPlayer.sendTitlePart(TitlePart.TITLE, it.language.child("gamelobby").getCmp(
                if (event.game.size.teamSize == 1) "end.won.single.title"
                else "end.won.title"
            ))
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