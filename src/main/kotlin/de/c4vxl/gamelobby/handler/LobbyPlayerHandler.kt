package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.GameLobby
import de.c4vxl.gamelobby.system.Lobby
import de.c4vxl.gamelobby.system.Lobby.isInLobby
import de.c4vxl.gamelobby.system.Lobby.sendToLobby
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

class LobbyPlayerHandler(plugin: Plugin) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onPlayerDamage(event: EntityDamageByEntityEvent) {
        if (!(event.entity as? Player ?: return).isInLobby) return
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
    fun onFallOff(event: PlayerMoveEvent) {
        if (!event.player.isInLobby) return
        if (Lobby.spawnLocation.world.minHeight + 3 < event.player.location.blockY) return

        event.player.sendToLobby()
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        val player: Player = event.whoClicked as? Player ?: return
        val item: ItemStack = event.currentItem ?: return
        if (!player.isInLobby) return
        if (player.gameMode == GameMode.CREATIVE) return
        if (item.itemMeta?.persistentDataContainer?.has(NamespacedKey(GameLobby.instance, "lobbyitem.action")) != true) return

        event.isCancelled = true
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (player.gameMode == GameMode.CREATIVE) return
        if (!player.isInLobby) return

        event.isCancelled = true
    }

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (!player.isInLobby) return
        val item: ItemStack = event.item ?: return
        val action: String = item.itemMeta?.persistentDataContainer?.get(NamespacedKey(GameLobby.instance, "lobbyitem.action"), PersistentDataType.STRING) ?: return

        when(action) {
            "tp_to_spawn" -> {
                if (!event.action.isRightClick) return
                if (player.getCooldown(item.type) > 0) return

                player.setCooldown(item.type, 20 * 1)

                player.sendToLobby()
            }
            "boost" -> {
                if (!event.action.isRightClick) return

                if (player.getCooldown(item.type) > 0) {
                    player.sendMessage(GameLobby.prefix.append(Component.text("Your booster hasn't recovered yet!").color(NamedTextColor.RED)))
                    return
                }

                player.setCooldown(item.type, 20 * 4)

                player.velocity = player.eyeLocation.direction.add(Vector(0.0, GameLobby.config.getDouble("booster.y_offset"), 0.0))
                    .multiply(GameLobby.config.getDouble("booster.strength"))
            }
            else -> return
        }

        event.isCancelled = true
    }
}