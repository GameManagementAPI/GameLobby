package de.c4vxl.gamelobby.gui

import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamelobby.utils.ScrollableInventory
import de.c4vxl.gamemanager.gma.game.Game
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * An interface for choosing a team
 */
class TeamChooser(
    private val player: Player,
    private val game: Game
) {
    val language = player.language.child("gamelobby")

    val items = game.teamManager.teams.values.map { team ->
        Item.invClickItem(
            ItemBuilder(
            if (team.players.isEmpty()) Material.GREEN_STAINED_GLASS_PANE
            else if (!team.isFull) Material.ORANGE_STAINED_GLASS_PANE
            else Material.RED_STAINED_GLASS_PANE,
            MiniMessage.miniMessage().deserialize(team.label),
            lore = buildList {
                add(language.getCmp("interface.team.item.lore.l1") as TextComponent)
                addAll(team.players.map { Component.text("- ${it.bukkitPlayer.name}").color(NamedTextColor.WHITE) })
                repeat(team.size - this.size + 1) {
                    add(Component.text("- ").color(NamedTextColor.WHITE).append(Component.text("/").decorate(TextDecoration.BOLD)))
                }
            }.toMutableList()
        )
        ) { event ->
            val player = event.whoClicked as? Player ?: return@invClickItem

            if (team.isFull) {
                player.sendMessage(language.getCmp("interface.team.msg.full"))
                player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);
                return@invClickItem
            }

            if (team.players.contains(player.gma))
                return@invClickItem

            // Quit old team
            game.teamManager.quit(player.gma)

            // Join team
            game.teamManager.join(player.gma, team.id)

            // Send message
            player.sendMessage(language.getCmp("interface.team.msg.success", team.label))

            // Reload page
            event.inventory.close()

            // Play sound
            player.playSound(player.location, Sound.BLOCK_SCAFFOLDING_PLACE, 1.0f, 1.0f);
        }
    }.toMutableList()

    /**
     * Opens the interface
     */
    fun open() = ScrollableInventory(
            items,
            language.getCmp("interface.team.title"),
            player
        ).open()
}