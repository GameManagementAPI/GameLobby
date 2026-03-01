package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.gui.MapVote
import de.c4vxl.gamelobby.gui.TeamChooser
import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamemanager.gma.event.game.GameStartEvent
import de.c4vxl.gamemanager.gma.event.game.GameStopEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerJoinedEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerQuitEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Handles players while in queue of a game
 */
class QueueHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onStart(event: GameStartEvent) {
        val mostVoted = MapVote.votes.getOrDefault(event.game, mutableMapOf())
            .mapValues { it.value.size }
            .map { it.value to it.key }
            .minByOrNull { it.first }?.second

        if (event.game.worldManager.forcemap == null)
            event.game.worldManager.forcemap = mostVoted
    }

    @EventHandler
    fun onStop(event: GameStopEvent) {
        MapVote.votes.remove(event.game)
    }

    @EventHandler
    fun onGameQuit(event: GamePlayerQuitEvent) {
        val game = event.game
        if (!game.isQueuing) return

        // Remove vote
        MapVote.votes.getOrPut(game) { mutableMapOf() }
            .forEach {
                it.value.remove(event.player.bukkitPlayer)
            }
    }

    @EventHandler
    fun onGameJoin(event: GamePlayerJoinedEvent) {
        val game = event.game
        if (!game.isQueuing) return

        val player = event.player.bukkitPlayer
        val language = event.player.language.child("gamelobby")

        player.inventory.clear()
        player.inventory.setItem(
            1,
            Item.rightClickItem(ItemBuilder(
                Material.valueOf(Main.config.getString("config.queue.team-selection-item") ?: ""),
                language.getCmp("queue.item.team.name")
            )) {
                player.playSound(player.location, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                TeamChooser(player, game).open()
            }
        )

        player.inventory.setItem(
            4,
            Item.rightClickItem(ItemBuilder(
                Material.valueOf(Main.config.getString("config.queue.map-voting-item") ?: ""),
                language.getCmp("queue.item.vote.name")
            )) {
                player.playSound(player.location, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                MapVote(player, game).open()
            }
        )

        player.inventory.setItem(
            7,
            Item.rightClickItem(ItemBuilder(
                Material.valueOf(Main.config.getString("config.queue.leave-item") ?: ""),
                language.getCmp("queue.item.leave.name")
            )) {
                it.player.gma.quit()
                player.playSound(player.location, Sound.BLOCK_BAMBOO_HIT, 1.0f, 1.0f);
            }
        )
    }
}