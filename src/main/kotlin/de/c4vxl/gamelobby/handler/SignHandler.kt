package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.GameLobby
import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class SignHandler(plugin: Plugin) : Listener {
    companion object {
        fun placeSign(location: Location, size: String): Boolean {
            val sign: Sign = (location.block.state as? Sign) ?: return false

            sign.setLine(0, GameLobby.config.getString("signs.line0")?.replace("\$GAMESIZE", size) ?: "")
            sign.setLine(1, GameLobby.config.getString("signs.line1")?.replace("\$GAMESIZE", size) ?: "")
            sign.setLine(2, GameLobby.config.getString("signs.line2")?.replace("\$GAMESIZE", size) ?: "")
            sign.setLine(3, GameLobby.config.getString("signs.line3")?.replace("\$GAMESIZE", size) ?: "")

            sign.persistentDataContainer.set(NamespacedKey(GameLobby.instance, "lobby.signs.gamesize"), PersistentDataType.STRING, size)
            sign.update(true, true)

            return true
        }
    }

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onSignInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = ((event.clickedBlock ?: return).state as? Sign ?: return)
        val size: String = block.persistentDataContainer.get(NamespacedKey(GameLobby.instance, "lobby.signs.gamesize"), PersistentDataType.STRING) ?: return
        val (teamAmount: Int, teamSize: Int) = size.split("x").map { it.toInt() }

        // get game with size
        val game: Game = GameManagementAPI.getGame(teamAmount, teamSize)

        // join game
        game.join(event.player.asGamePlayer)
    }
}