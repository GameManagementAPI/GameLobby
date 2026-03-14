package de.c4vxl.gamelobby.events.signs

import de.c4vxl.gamelobby.events.type.LobbyPlayerEvent
import org.bukkit.block.Sign
import org.bukkit.entity.Player

/**
 * Triggered when a player clicks a sign in the lobby
 * @param player The player that clicked
 */
data class LobbyPlayerSignClickEvent(override val player: Player, val sign: Sign) : LobbyPlayerEvent(player)