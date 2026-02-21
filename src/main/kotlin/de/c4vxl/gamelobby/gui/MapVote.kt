package de.c4vxl.gamelobby.gui

import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamelobby.utils.ScrollableInventory
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * An interface for choosing a team
 */
class MapVote(
    private val player: Player,
    private val game: Game
) {
    companion object {
        val votes = mutableMapOf<Game, MutableMap<String, MutableList<Player>>>()
    }

    val language = player.language.child("gamelobby")

    val items = game.worldManager.availableMaps.map { map ->
        val builders = map.builders
        val item = Material.entries.find { it.name.contentEquals(map.metadata.getString("displayItem")) } ?: Material.MAP

        val mapVotes = votes.getOrPut(game) { mutableMapOf() }

        Item.invClickItem(
            ItemBuilder(
                item,
                Component.text(map.name),
                lore = buildList {
                    add(language.getCmp("interface.maps.item.lore.l1") as TextComponent)
                    add(Component.text(builders.joinToString("/")).color(NamedTextColor.WHITE))
                    add(Component.empty())
                    add(language.getCmp("interface.maps.item.lore.l2", mapVotes.getOrDefault(map.name, mutableListOf()).size.toString()) as TextComponent)
                }.toMutableList()
            )
        ) { event ->
            val player = event.whoClicked as? Player ?: return@invClickItem

            if (mapVotes.values.flatten().contains(player)) {
                player.sendMessage(language.getCmp("interface.maps.msg.already"))
                player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
                return@invClickItem
            }

            // Vote
            mapVotes.getOrPut(map.name) { mutableListOf() }
                .add(player)

            // Send message
            player.sendMessage(language.getCmp("interface.maps.msg.success", map.name))

            // Play sound
            player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_PLACE, 1.0f, 1.0f);

            event.inventory.close()
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