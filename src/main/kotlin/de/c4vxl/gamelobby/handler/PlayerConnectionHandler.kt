package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.GameLobby
import de.c4vxl.gamelobby.system.Lobby.sendToLobby
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class PlayerConnectionHandler(plugin: Plugin) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // send message
        event.player.sendMessage(GameLobby.prefix.append(Component.text("Welcome back to the lobby!").color(NamedTextColor.GRAY)))

        event.player.sendToLobby()
        event.joinMessage(null)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.player.sendToLobby()
        event.quitMessage(null)
    }
}