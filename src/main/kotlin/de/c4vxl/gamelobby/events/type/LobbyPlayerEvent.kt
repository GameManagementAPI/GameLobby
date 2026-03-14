package de.c4vxl.gamelobby.events.type

import org.bukkit.entity.Player

/**
 * Base class of player-specific lobby-events
 */
open class LobbyPlayerEvent(open val player: Player) : LobbyEvent()