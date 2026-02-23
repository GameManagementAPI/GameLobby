package de.c4vxl.gamelobby.gui

import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamelobby.utils.ScrollableInventory
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.w3c.dom.Text

/**
 * Interface for creating private games
 */
class PrivateGame(
    private val player: Player
) {
    val language = player.language.child("gamelobby")

    val items = GMA.possibleGameSizes.mapNotNull { size ->
        val gameSize = GameSize.fromString(size) ?: return@mapNotNull null
        Item.invClickItem(ItemBuilder(
            Material.STRING,
            name = language.getCmp("interface.private-game.item.name", size),
            lore = mutableListOf(
                language.getCmp("interface.private-game.item.lore.l1") as TextComponent,
                language.getCmp("interface.private-game.item.lore.l2", gameSize.teamAmount.toString()) as TextComponent,
                language.getCmp("interface.private-game.item.lore.l3") as TextComponent,
                language.getCmp("interface.private-game.item.lore.l4", gameSize.teamSize.toString()) as TextComponent,
            )
        )) {
            player.closeInventory()

            if (player.gma.isInGame)
                return@invClickItem

            val game = GMA.createGame(gameSize, player.gma)

            // Join
            player.gma.join(game)

            player.sendMessage(language.getCmp("interface.private-game.success", size))
        }
    }.toMutableList()

    /**
     * Opens the interface
     */
    fun open() = ScrollableInventory(
        items,
        language.getCmp("interface.private-game.title"),
        player
    ).open()
}