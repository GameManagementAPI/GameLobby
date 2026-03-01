package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.lobby.Lobby
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Handles player visibility in the lobby
 */
class VisibilityHandler {
    init {
        Bukkit.getScheduler().runTaskTimer(Main.instance, Runnable {
            Bukkit.getOnlinePlayers().forEach { handle(it) }
        }, 10, 10)
    }

    /**
     * Handles visibility for one player
     */
    private fun handle(self: Player) {
        Bukkit.getOnlinePlayers().forEach { other ->
            if (Lobby.showPlayers(self) || (self.gma.isInGame || self.world.name != Lobby.spawn.world.name))
                self.showPlayer(Main.instance, other)
            else
                self.hidePlayer(Main.instance, other)
        }
    }
}