package de.c4vxl.gamelobby.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ScrollableInventory(
    private val items: MutableList<ItemStack>,
    private val title: String,
    private val invSize: Int = 9 * 1,
    private val itemsPerPage: Int = invSize-2
) {
    private val totalPages: Int
        get() = (items.size + itemsPerPage - 1) / itemsPerPage

    fun page(page: Int): Inventory {
        val inventory = Bukkit.createInventory(null, invSize, LegacyComponentSerializer.legacySection().deserialize(title))

        // Add navigation arrows
        if (page > 0) {
            val previousPageItem = ItemBuilder(
                material = Material.ARROW,
                name = Component.text("Previous Page")
            ).apply {
                invClickHandler = { event: InventoryClickEvent ->
                    if (page > 0) {
                        event.whoClicked.openInventory(page(page - 1))
                        event.isCancelled = true
                    }
                }
            }.build()
            inventory.setItem(0, previousPageItem)
        }

        if (page < totalPages - 1) {
            val nextPageItem = ItemBuilder(
                material = Material.ARROW,
                name = Component.text("Next Page")
            ).apply {
                invClickHandler = { event: InventoryClickEvent ->
                    if (page < totalPages - 1) {
                        event.whoClicked.openInventory(page(page + 1))
                        event.isCancelled = true
                    }
                }
            }.build()
            inventory.setItem(invSize-1, nextPageItem)
        }

        // Add items for the current page
        val start = page * itemsPerPage
        val end = (start + itemsPerPage).coerceAtMost(items.size)

        for (i in start until end) {
            inventory.setItem(i - start + 1, items[i])
        }

        return inventory
    }
}