package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.lobby.Lobby
import de.c4vxl.gamelobby.lobby.Lobby.isInLobby
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

/**
 * Handles players while in lobby
 */
class LobbyHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return

        if (!player.isInLobby) return
        if ((event.damager as? Player)?.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true
    }

    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        if (!event.player.isInLobby) return
        if (event.player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true
    }

    @EventHandler
    fun onFoodChange(event: FoodLevelChangeEvent) {
        if (!(event.entity as? Player ?: return).isInLobby) return

        event.isCancelled = true
    }

    @EventHandler
    fun onFallOutOfWorld(event: PlayerMoveEvent) {
        if (!event.player.isInLobby) return
        if (Lobby.spawn.world.minHeight + 3 < event.player.location.blockY) return

        event.player.teleport(Lobby.spawn)
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return

        if (!player.isInLobby) return
        if (player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true
    }

    @EventHandler
    fun onOffhandSwap(event: PlayerSwapHandItemsEvent) {
        if (!event.player.isInLobby) return
        if (event.player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!player.isInLobby) return
        if (player.gameMode == GameMode.CREATIVE) return

        event.isCancelled = true
    }
}