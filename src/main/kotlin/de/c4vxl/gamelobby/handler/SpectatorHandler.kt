package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.gui.SpectatorTeleporter
import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.event.player.GamePlayerSpectateEndEvent
import de.c4vxl.gamemanager.gma.event.player.GamePlayerSpectateStartEvent
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Custom spectator mode for games
 */
class SpectatorHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onSpectate(event: GamePlayerSpectateStartEvent) {
        val language = event.player.language.child("gamelobby")

        // Reset player
        event.player.reset()
        event.player.bukkitPlayer.let { player ->
            player.gameMode = GameMode.SURVIVAL

            // Allow flight
            player.allowFlight = true
            player.isFlying = true

            player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 99999, 255, true, false, false))

            // Add to spectator team
            val team = player.scoreboard.let { scoreboard ->
                scoreboard.getTeam("gma_spec_${event.game.id}")
                    ?: scoreboard.registerNewTeam("gma_spec_${event.game.id}")
            }
            team.addPlayer(player)
            team.prefix(language.getCmp("spectator.prefix"))

            // Teleporter
            player.inventory.setItem(1, Item.rightClickItem(
                ItemBuilder(
                    Material.COMPASS,
                    language.getCmp("spectator.item.compass.name")
                )
            ) { SpectatorTeleporter(player, event.game).open() })

            // Next game item
            player.inventory.setItem(7, Item.rightClickItem(
                ItemBuilder(
                    Material.ARROW,
                    language.getCmp("spectator.item.next-game.name")
                )
            ) {
                it.player.gma.quit()
                it.player.gma.join(GMA.getOrCreate(event.game.size))
            })
        }
    }

    @EventHandler
    fun onQuit(event: GamePlayerSpectateEndEvent) {
        val team = event.player.bukkitPlayer.scoreboard.getTeam("gma_spec_${event.game.id}")

        if (team?.hasPlayer(event.player.bukkitPlayer) == true)
            team.removePlayer(event.player.bukkitPlayer)

        // Reset player
        event.player.reset()
        event.player.bukkitPlayer.let {
            it.activePotionEffects.forEach { e -> it.removePotionEffect(e.type) }
            it.isFlying = false
            it.allowFlight = false
        }
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!player.gma.isSpectating) return

        event.isCancelled = true
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return

        if (!damager.gma.isSpectating) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!event.player.gma.isSpectating) return

        event.isCancelled = true
    }

    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        if (!event.player.gma.isSpectating) return

        event.isCancelled = true
    }

    @EventHandler
    fun onItemPickup(event: PlayerAttemptPickupItemEvent) {
        if (!event.player.gma.isSpectating) return

        event.isCancelled = true
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (!player.gma.isSpectating) return

        event.isCancelled = true
    }
}