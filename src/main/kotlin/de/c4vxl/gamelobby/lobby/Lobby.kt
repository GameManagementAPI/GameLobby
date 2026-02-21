package de.c4vxl.gamelobby.lobby

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Main interface for lobby actions
 */
object Lobby {
    /**
     * Holds the spawn location
     */
    var spawn: Location
        get() = Main.config.getLocation("game.spawn") ?: Bukkit.getWorlds().first().spawnLocation
        set(value) {
            Main.config.set("game.spawn", value)
            Main.instance.saveConfig()
        }

    /**
     * Sends a player to the lobby
     * @param player The player
     */
    fun send(player: Player) {
        // Reset player
        player.gma.reset()
        player.gameMode = GameMode.SURVIVAL

        player.teleport(spawn)
    }
}