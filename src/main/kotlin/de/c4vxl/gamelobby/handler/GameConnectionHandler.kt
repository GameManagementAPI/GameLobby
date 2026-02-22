package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.lobby.Lobby
import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerSpectateEndEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Handles players connecting to games
 */
class GameConnectionHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onGameJoin(event: GamePlayerJoinedEvent) {
        // Teleport to spawn
        Lobby.send(event.player.bukkitPlayer)
    }

    @EventHandler
    fun onGameQuit(event: GamePlayerQuitEvent) {
        // Teleport to spawn
        Lobby.send(event.player.bukkitPlayer)
    }

    @EventHandler
    fun onSpectatorQuit(event: GamePlayerSpectateEndEvent) {
        Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
            // Teleport to spawn
            Lobby.send(event.player.bukkitPlayer)
        }, 10L)
    }
}