package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.lobby.Lobby
import de.c4vxl.gamemanager.language.Language.Companion.language
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ConnectionHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        // Move to lobby
        Lobby.send(event.player)

        // Send welcome message
        if (Main.config.getBoolean("config.send-welcome-message", true))
            event.player.sendMessage(
                event.player.language.child("gamelobby")
                    .getCmp("lobby.join.welcome", event.player.name)
            )

        // Hide join message
        event.joinMessage(null)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        // Hide quit message
        event.quitMessage(null)
    }
}