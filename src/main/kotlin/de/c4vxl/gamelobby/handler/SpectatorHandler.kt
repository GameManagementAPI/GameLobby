package de.c4vxl.gamelobby.handler

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import de.c4vxl.gamelobby.utils.ComponentCollection
import de.c4vxl.gamelobby.utils.ItemBuilder
import de.c4vxl.gamelobby.utils.ScrollableInventory
import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import de.c4vxl.gamemanager.gamemanagementapi.event.GameSpectateStartEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameSpectateStopEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStopEvent
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SpectatorHandler(plugin: Plugin): Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onSpecQuit(event: GameSpectateStopEvent) {
        Bukkit.getScoreboardManager().mainScoreboard.getTeam("gma_spec_${event.game.id.asString}")?.removePlayer(event.player.bukkitPlayer)
        event.player.bukkitPlayer.clearActivePotionEffects()
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        Bukkit.getScoreboardManager().mainScoreboard.getTeam("gma_spec_${event.game.id.asString}")?.unregister()
    }

    @EventHandler
    fun onItemPickup(event: PlayerAttemptPickupItemEvent) {
        val player = event.player

        if (!player.asGamePlayer.isSpectating) return

        event.isCancelled = true
    }

    @EventHandler
    fun onSpecJoin(event: GameSpectateStartEvent) {
        val player = event.player.bukkitPlayer
        val world = event.game.worldManager.world ?: return
        player.gameMode = GameMode.ADVENTURE
        player.allowFlight = true
        player.inventory.clear()

        // make player kinda transparent
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 999999, 255, true, false, false))
        val team = Bukkit.getScoreboardManager().mainScoreboard.let { it.getTeam("gma_spec_${event.game.id.asString}") ?: it.registerNewTeam("gma_spec_${event.game.id.asString}") }
        team.addPlayer(player)
        team.prefix(Component.text("Spectator | "))

        player.inventory.setItem(1, ItemBuilder(
            Material.COMPASS,
            name = Component.text("Teleporter").color(NamedTextColor.GREEN).append(ComponentCollection.RIGHT_CLICK.component),
            invClickHandler = { it.isCancelled = true },
            interactonHandler = {
                it.isCancelled = true
                if (!it.action.isRightClick) return@ItemBuilder

                player.openInventory(ScrollableInventory(event.game.alivePlayers.map { gplayer ->
                    val ib = ItemBuilder(
                        material = Material.PLAYER_HEAD,
                        name = Component.text(gplayer.bukkitPlayer.name),
                        lore = mutableListOf(Component.text("Team:"), Component.text(gplayer.team?.name ?: "None").color(NamedTextColor.WHITE)),
                        invClickHandler = { event: InventoryClickEvent ->
                            val player = event.whoClicked as? Player ?: return@ItemBuilder

                            event.isCancelled = true

                            if (gplayer.bukkitPlayer.world.name != player.world.name) return@ItemBuilder

                            player.teleport(gplayer.bukkitPlayer.location)

                            player.closeInventory()
                        }
                    )

                    ib.build().apply {
                        val meta = this.itemMeta as? SkullMeta ?: return@apply
                        meta.playerProfile = Bukkit.createProfile(gplayer.bukkitPlayer.uniqueId).apply { setTextures(null) }
                        this.setItemMeta(meta)

                        ItemBuilder.inventoryClickHandlers[this.hashCode()] = ib.invClickHandler!!
                    }
                }.toMutableList(), "§6§lTeleporter").page(0))
            }).build())

        player.inventory.setItem(7, ItemBuilder(
            Material.ARROW,
            name = Component.text("Next game"),
            invClickHandler = { it.isCancelled = true },
            interactonHandler = {
                it.isCancelled = true
                it.player.asGamePlayer.quitGame()
                it.player.asGamePlayer.joinGame(GameManagementAPI.getGame(event.game.teamAmount, event.game.teamSize))
            }
        ).build())
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val damager: Player = event.damager as? Player ?: return
        val player: Player = event.entity as? Player ?: return

        if (!damager.asGamePlayer.isSpectating) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (!player.asGamePlayer.isSpectating) return

        event.isCancelled = true
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        if (!event.player.asGamePlayer.isSpectating) return
        event.player.asGamePlayer.game?.worldManager?.world?.spawnLocation?.let { event.respawnLocation = it }
    }

    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        if (!event.player.asGamePlayer.isSpectating) return
        event.isCancelled = true
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (!player.asGamePlayer.isSpectating) return
        event.isCancelled = true
    }
}