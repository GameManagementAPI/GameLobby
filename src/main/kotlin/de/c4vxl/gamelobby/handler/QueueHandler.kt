package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.utils.ComponentCollection
import de.c4vxl.gamelobby.utils.ItemBuilder
import de.c4vxl.gamelobby.utils.ScrollableInventory
import de.c4vxl.gamemanager.gamemanagementapi.event.GamePlayerJoinEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStartEvent
import de.c4vxl.gamemanager.gamemanagementapi.event.GameStopEvent
import de.c4vxl.gamemanager.gamemanagementapi.game.Game
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import de.c4vxl.gamemanager.gamemanagementapi.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.io.File

class QueueHandler(plugin: Plugin) : Listener {
    companion object {
        private val votes: MutableMap<Game, MutableMap<String, MutableList<GMAPlayer>>> = mutableMapOf()

        fun getVoters(game: Game, map: String): MutableList<GMAPlayer> {
            return votes.getOrPut(game) { mutableMapOf() }.getOrPut(map) { mutableListOf() }
        }

        fun getVote(game: Game, player: GMAPlayer): String? {
            game.worldManager.availableMaps.forEach {
                if (getVoters(game, it).contains(player)) return it
            }
            return null
        }

        fun vote(game: Game, map: String, player: GMAPlayer): Boolean {
            if (getVote(game, player) != null) return false

            val v = getVoters(game, map)
            if (!v.add(player)) return false
            votes[game]?.put(map, v)
            return true
        }

        fun getMaxVotes(game: Game): String? {
            val mapVotes = votes[game] ?: return null  // Get the map votes for the game, or return null if no votes exist

            // Find the map with the maximum number of votes
            val maxVotesMap = mapVotes.maxByOrNull { (_, voters) -> voters.size }

            return maxVotesMap?.key  // Return the map name (key) with the most votes
        }

    }

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onGameStart(event: GameStartEvent) {
        val game: Game = event.game
        if (!game.isQueuing) return

        // handle vote
        game.worldManager.forcemap = getMaxVotes(game)
    }

    @EventHandler
    fun onGameStop(event: GameStopEvent) {
        val game: Game = event.game
        votes.remove(game)
    }

    @EventHandler
    fun onGameJoin(event: GamePlayerJoinEvent) {
        val game: Game = event.game
        if (!game.isQueuing) return

        val player: Player = event.player.bukkitPlayer

        player.inventory.setItem(1, ItemBuilder(Material.WHITE_BANNER, Component.text("Select team").append(ComponentCollection.RIGHT_CLICK.component),
            interactonHandler = { event: PlayerInteractEvent ->
                val player: Player = event.player
                if (!event.action.isRightClick) return@ItemBuilder
                if (event.player.asGamePlayer.game != game || event.player.asGamePlayer.game?.isQueuing != true) return@ItemBuilder
                val items: MutableList<ItemStack> = game.teamManager.teams.map { team ->
                    ItemBuilder(if (team.players.isEmpty()) Material.GREEN_STAINED_GLASS_PANE
                                else if (!team.isFull) Material.ORANGE_STAINED_GLASS_PANE
                                else Material.RED_STAINED_GLASS_PANE,
                        LegacyComponentSerializer.legacySection().deserialize(team.name),
                        lore = mutableListOf(Component.text("Players: ").decorate(TextDecoration.BOLD)).apply {
                            if (team.players.isNotEmpty()) this.addAll(team.players.map { Component.text("- ${it.bukkitPlayer.name}").color(NamedTextColor.WHITE) })
                            else this.add(Component.text("None").color(NamedTextColor.WHITE))
                        },
                        invClickHandler = { event: InventoryClickEvent ->
                            val player: Player = event.whoClicked as? Player ?: return@ItemBuilder
                            event.isCancelled = true
                            if (team.isFull) {
                                player.sendMessage(
                                    ComponentCollection.PREFIX.component.append(ComponentCollection.SORRY.component)
                                        .append(Component.text("This team seems to be full already!"))
                                )

                                player.closeInventory()
                            } else if (!team.players.contains(player.asGamePlayer)) {
                                if (player.asGamePlayer.isInTeam) player.asGamePlayer.team?.quit(player.asGamePlayer)
                                game.teamManager.join(player.asGamePlayer, team)

                                // send
                                player.sendMessage(ComponentCollection.PREFIX.component.append(
                                    Component.text("Successfully selected team ").color(NamedTextColor.GREEN)
                                ).append(LegacyComponentSerializer.legacySection().deserialize(team.name)))
                                player.closeInventory()
                            }
                        }
                    ).build()
                }.toMutableList()

                player.openInventory(ScrollableInventory(items, "§6§lSelect a team").page(0))
            }
        ).build())

        player.inventory.setItem(4, ItemBuilder(Material.MAP, Component.text("Vote for map").append(ComponentCollection.RIGHT_CLICK.component),
            interactonHandler = { event: PlayerInteractEvent ->
                if (!event.action.isRightClick) return@ItemBuilder
                val player: Player = event.player
                if (event.player.asGamePlayer.game != game || event.player.asGamePlayer.game?.isQueuing != true) return@ItemBuilder

                val items: MutableList<ItemStack> = game.worldManager.availableMaps.map {
                    val config = YamlConfiguration.loadConfiguration(File(WorldManager.mapsContainerPath, "${game.gameSize}/$it/mapdata.yml"))
                    val builder: String = config.getString("map.builder") ?: "unknown"
                    val displayItem: Material = Material.entries.find { it.name.contentEquals(config.getString("map.item") ?: "MAP") } ?: Material.MAP

                    ItemBuilder(displayItem,
                        Component.text(it),
                        lore = mutableListOf(Component.text("Builder: "),
                            Component.text(builder).color(NamedTextColor.GRAY),
                            Component.text(""),
                            Component.text("Votes:"),
                            Component.text(getVoters(game, it).size.toString()).color(NamedTextColor.WHITE)),
                        invClickHandler = { event: InventoryClickEvent ->
                            val player: Player = event.whoClicked as? Player ?: return@ItemBuilder
                            event.isCancelled = true

                            if (!vote(game, it, player.asGamePlayer)) player.sendMessage(
                                ComponentCollection.PREFIX.component.append(ComponentCollection.SORRY.component)
                                    .append(Component.text("But you can only vote once per game!"))
                            )
                            else player.sendMessage(
                                ComponentCollection.PREFIX.component
                                    .append(Component.text("Successfully voted for ")
                                        .append(Component.text(it).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)))
                            )
                            player.closeInventory()
                        }
                    ).build()
                }.toMutableList()

                player.openInventory(ScrollableInventory(items, "§6§lVote for a map").page(0))
            }
        ).build())

        player.inventory.setItem(7, ItemBuilder(Material.OAK_DOOR, Component.text("Quit").color(NamedTextColor.RED).append(ComponentCollection.RIGHT_CLICK.component),
            interactonHandler = { event: PlayerInteractEvent ->
                if (!event.action.isRightClick) return@ItemBuilder
                if (event.player.asGamePlayer.game != game || event.player.asGamePlayer.game?.isQueuing != true) return@ItemBuilder
                event.player.asGamePlayer.quitGame()
            }).build()
        )
    }
}