package de.c4vxl.gamelobby.utils

import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object Item {
    fun rightClickItem(
        builder: ItemBuilder,
        handler: (PlayerInteractEvent) -> Unit
    ): ItemStack =
        builder.onEvent(
            PlayerInteractEvent::class.java,
            object : ItemBuilder.ItemEventHandler<PlayerInteractEvent> {
                override fun handle(event: PlayerInteractEvent) {
                    event.isCancelled = true

                    if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK)
                        return

                    handler(event)
                }
            }
        ).build()

    fun invClickItem(
        builder: ItemBuilder,
        handler: (InventoryClickEvent) -> Unit
    ): ItemStack =
        builder.onEvent(
            InventoryClickEvent::class.java,
            object : ItemBuilder.ItemEventHandler<InventoryClickEvent> {
                override fun handle(event: InventoryClickEvent) {
                    event.isCancelled = true

                    handler(event)
                }
            }
        ).build()
}