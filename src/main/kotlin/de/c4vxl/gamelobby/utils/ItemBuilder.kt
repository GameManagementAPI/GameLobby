package de.c4vxl.gamelobby.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class ItemBuilder(
    var material: Material,
    var name: Component = Component.text(material.name),
    var amount: Int = 1,
    var lore: MutableList<TextComponent> = mutableListOf(),
    var unbreakable: Boolean = false,
    var enchantments: MutableMap<Enchantment, Int> = mutableMapOf(),
    var interactonHandler: ((PlayerInteractEvent) -> Unit)? = null,
    var invClickHandler: ((InventoryClickEvent) -> Unit)? = null) {
    companion object : Listener {
        private val interactionHandlers: MutableMap<Int, (PlayerInteractEvent) -> Unit> = mutableMapOf()
        private val inventoryClickHandlers: MutableMap<Int, (InventoryClickEvent) -> Unit> = mutableMapOf()

        fun register(plugin: Plugin) {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }

        @EventHandler
        fun onItemInteraction(event: PlayerInteractEvent) {
            val currentItem: ItemStack = event.item ?: return
            interactionHandlers[currentItem.hashCode()]?.invoke(event)
        }

        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) {
            val currentItem: ItemStack = event.currentItem ?: return
            inventoryClickHandlers[currentItem.hashCode()]?.invoke(event)
        }
    }

    fun build(): ItemStack {
        val itemStack = ItemStack(material, amount).apply {
            this.editMeta { meta ->
                meta.displayName(this@ItemBuilder.name)
                meta.lore(this@ItemBuilder.lore)
                meta.isUnbreakable = this@ItemBuilder.unbreakable
            }

            this.addEnchantments(this@ItemBuilder.enchantments)
        }

        interactonHandler?.let { interactionHandlers[itemStack.hashCode()] = it }
        invClickHandler?.let { inventoryClickHandlers[itemStack.hashCode()] = it }

        return itemStack
    }
}