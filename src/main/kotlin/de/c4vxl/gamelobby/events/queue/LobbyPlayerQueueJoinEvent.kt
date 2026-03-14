package de.c4vxl.gamelobby.events.queue

import de.c4vxl.gamelobby.events.type.LobbyPlayerEvent
import org.bukkit.entity.Player

/**
 * Triggered when a player joins a lobby queue
 * @param player The player that joined
 * @param equipItems If set to {@code false} player won't receive any items
 */
data class LobbyPlayerQueueJoinEvent(override val player: Player, val equipItems: Boolean = true) : LobbyPlayerEvent(player)