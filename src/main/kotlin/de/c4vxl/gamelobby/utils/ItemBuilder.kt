package de.c4vxl.gamelobby.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class ItemBuilder(
    var material: Material,
    var name: Component? = null,
    var amount: Int = 1,
    var lore: MutableList<TextComponent> = mutableListOf(),
    var unbreakable: Boolean = false,
    var enchantments: MutableMap<Enchantment, Int> = mutableMapOf(),
    var interactonHandler: ((PlayerInteractEvent) -> Unit)? = null,
    var invClickHandler: ((InventoryClickEvent) -> Unit)? = null,
    val eventKey: String = "",
    var itemMeta: ItemMeta? = null
) {
    companion object : Listener {
        val interactionHandlers: MutableMap<String, (PlayerInteractEvent) -> Unit> = mutableMapOf()
        val inventoryClickHandlers: MutableMap<String, (InventoryClickEvent) -> Unit> = mutableMapOf()

        fun register(plugin: Plugin) {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }

        @EventHandler
        fun onItemInteraction(event: PlayerInteractEvent) {
            val currentItem: ItemStack = event.item ?: return
            val meta = currentItem.itemMeta ?: return
            val key = meta.persistentDataContainer.get(NamespacedKey.minecraft("itembuilder"), PersistentDataType.STRING) ?: return

            interactionHandlers[key]?.invoke(event)
        }

        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) {
            val currentItem: ItemStack = event.currentItem ?: return
            val meta = currentItem.itemMeta ?: return
            val key = meta.persistentDataContainer.get(NamespacedKey.minecraft("itembuilder"), PersistentDataType.STRING) ?: return

            inventoryClickHandlers[key]?.invoke(event)
        }
    }

    fun build(): ItemStack {
        val key = material.name + (name?.toString() ?: "") + enchantments.hashCode().toString() + eventKey
        val itemStack = ItemStack(material, amount).apply {
            this@ItemBuilder.itemMeta = this@ItemBuilder.itemMeta ?: this.itemMeta
            if (this@ItemBuilder.name != null) this@ItemBuilder.itemMeta!!.displayName(this@ItemBuilder.name)
            this@ItemBuilder.itemMeta!!.lore(this@ItemBuilder.lore)
            this@ItemBuilder.itemMeta!!.isUnbreakable = this@ItemBuilder.unbreakable
            // Store the consistent key in the PersistentDataContainer
            this@ItemBuilder.itemMeta!!.persistentDataContainer.set(NamespacedKey.minecraft("itembuilder"), PersistentDataType.STRING, key)
            this.itemMeta = this@ItemBuilder.itemMeta

            this.addUnsafeEnchantments(this@ItemBuilder.enchantments)
        }

        interactonHandler?.let { interactionHandlers[key] = it }
        invClickHandler?.let { inventoryClickHandlers[key] = it }

        return itemStack
    }
}