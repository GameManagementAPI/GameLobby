package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.system.Lobby
import de.c4vxl.gamelobby.system.Lobby.sendToLobby
import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerJoinEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStopEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class GameConnectionHandler(plugin: Plugin) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerJoinGame(event: GamePlayerJoinEvent) {
        event.player.bukkitPlayer.teleport(Lobby.spawnLocation) // tp player to spawn
        event.player.bukkitPlayer.inventory.clear()
    }

    @EventHandler
    fun onPlayerQuitGame(event: GamePlayerQuitEvent) {
        event.player.sendToLobby() // send to lobby
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        event.kickPlayers = false
        event.game.worldManager.world?.players?.forEach { it.sendToLobby() }
    }
}