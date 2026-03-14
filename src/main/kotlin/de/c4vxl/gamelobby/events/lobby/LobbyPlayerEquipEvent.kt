package de.c4vxl.gamelobby.events.lobby

import de.c4vxl.gamelobby.events.type.LobbyPlayerEvent
import org.bukkit.entity.Player

/**
 * Triggered when a player gets equipped with the lobby items
 * @param player The player
 */
data class LobbyPlayerEquipEvent(override val player: Player) : LobbyPlayerEvent(player)