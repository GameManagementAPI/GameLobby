package de.c4vxl.gamelobby.events.queue

import de.c4vxl.gamelobby.events.type.LobbyPlayerEvent
import org.bukkit.entity.Player

/**
 * Triggered after a player has joined a lobby queue
 * @param player The player that joined
 */
data class LobbyPlayerQueueJoinedEvent(override val player: Player) : LobbyPlayerEvent(player)