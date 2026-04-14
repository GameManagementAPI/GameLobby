package de.c4vxl.gamelobby.utils

import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * A scrollable interface made with inventories
 */
class ScrollableInventory(
    private val items: MutableList<ItemStack>,
    private val title: Component,
    private val player: Player,
    private val numRows: Int = 1
) {
    private val itemsPerPage: Int = numRows * 7
    private val totalPages: Int = (items.size + itemsPerPage - 1) / itemsPerPage

    /**
     * Returns a specific page
     * @param page The page
     */
    private fun page(page: Int): Inventory {
        val inventory = Bukkit.createInventory(null, 9 * (numRows + 2), title)

        // Add filler items
        val filler = Item.invClickItem(ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, Component.empty())) {}
        val lastRowFirstSlot = inventory.size - 9
        for (i in 0..8) inventory.setItem(i, filler)
        for (i in 0..lastRowFirstSlot step 9) inventory.setItem(i, filler)
        for (i in 8..<inventory.size step 9) inventory.setItem(i, filler)
        for (i in lastRowFirstSlot..<inventory.size) inventory.setItem(i, filler)

        // Navigation arrows
        if (page > 0)
            inventory.setItem(0, Item.invClickItem(ItemBuilder(
                Material.ARROW,
                player.language.child("gamelobby").getCmp("scrollable.arrow.back")
            )) { open(page - 1) })

        if (page < totalPages - 1)
            inventory.setItem(8, Item.invClickItem(ItemBuilder(
                Material.ARROW,
                player.language.child("gamelobby").getCmp("scrollable.arrow.next")
            )) {
                if (page < totalPages - 1)
                    open(page + 1)
            })

        // Add items to page
        val start = page * itemsPerPage
        items
            .drop(start)
            .take(itemsPerPage)
            .forEach { inventory.addItem(it) }

        return inventory
    }

    /**
     * Opens the inventory to a player
     * @param page The page to open
     */
    fun open(page: Int = 0) {
        player.openInventory(page(page))
    }
}