package de.c4vxl.gamelobby.gui

import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamelobby.utils.ScrollableInventory
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta

/**
 * An interface for choosing who to teleport to
 */
class SpectatorTeleporter(
    private val player: Player,
    private val game: Game
) {
    val language = player.language.child("gamelobby")

    val items = game.playerManager.alivePlayers.map { p ->
        Item.invClickItem(ItemBuilder(
            Material.PLAYER_HEAD,
            language.getCmp("spectator.compass.item.name", p.bukkitPlayer.name),
            lore = mutableListOf(
                language.getCmp("spectator.compass.item.lore.l1") as TextComponent,
                language.getCmp("spectator.compass.item.lore.l2", p.team?.label ?: "/") as TextComponent
            )
        )) {
            if (!player.gma.isSpectating)
                return@invClickItem

            if (p.game != player.gma.game)
                return@invClickItem

            player.teleport(p.bukkitPlayer.location)
            player.closeInventory()
        }.apply {
            val meta = this.itemMeta as? SkullMeta ?: return@apply
            meta.playerProfile = Bukkit.createProfile(p.bukkitPlayer.uniqueId)
            meta.itemName(language.getCmp("spectator.compass.item.name", p.bukkitPlayer.name))
            this.setItemMeta(meta)
        }
    }.toMutableList()

    /**
     * Opens the interface
     */
    fun open() = ScrollableInventory(
            items,
            language.getCmp("interface.maps.title"),
            player
        ).open()
}