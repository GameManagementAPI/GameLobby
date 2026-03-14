package de.c4vxl.gamelobby.events.lobby

import de.c4vxl.gamelobby.events.type.LobbyPlayerEvent
import org.bukkit.entity.Player

/**
 * Triggered when a player gets send to the lobby
 * @param player The player
 */
data class LobbyPlayerSendEvent(override val player: Player) : LobbyPlayerEvent(player)