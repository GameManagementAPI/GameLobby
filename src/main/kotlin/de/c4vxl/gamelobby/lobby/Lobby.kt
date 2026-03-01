package de.c4vxl.gamelobby.lobby

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.gui.PrivateGame
import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.*
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.file.YamlConstructor
import org.bukkit.entity.Player
import java.io.File

/**
 * Main interface for lobby actions
 */
object Lobby {
    /**
     * Holds the spawn location
     */
    var spawn: Location
        get() = (Main.config.getLocation("game.spawn") ?: Bukkit.getWorlds().first().spawnLocation).apply {
            this.world.setGameRule(GameRules.KEEP_INVENTORY, true)
            this.world.setGameRule(GameRules.ADVANCE_TIME, false)
            this.world.setGameRule(GameRules.ADVANCE_WEATHER, false)
            this.world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true)
            this.world.setGameRule(GameRules.FALL_DAMAGE, false)
        }
        set(value) {
            Main.config.set("game.spawn", value)
            Main.config.save(Main.instance.dataFolder.resolve("config.yml"))
        }


    /**
     * Returns the player preference for showing other players
     * @param player The player
     * @param newValue If set, new value will be saved
     */
    fun showPlayers(player: Player, newValue: Boolean? = null): Boolean {
        // Get config
        val configFile = File(Main.config.getString("config.lobby.visibility-db") ?: "player-visibility.yml")
        val config = YamlConfiguration.loadConfiguration(configFile)

        // Update value
        newValue?.let {
            config.set(player.uniqueId.toString(), it)
            config.save(configFile)
        }

        // Return config value
        return config.getBoolean(player.uniqueId.toString(), true)
    }

    /**
     * Sends a player to the lobby
     * @param player The player
     */
    fun send(player: Player) {
        // Reset player
        player.gma.reset()
        player.gameMode = GameMode.SURVIVAL

        // Teleport
        player.teleport(spawn)

        // Give items
        equipItems(player)
    }

    private fun equipItems(player: Player) {
        player.inventory.setItem(
            1,
            Item.rightClickItem(ItemBuilder(
                Material.ENDER_PEARL,
                player.language.child("gamelobby").getCmp("lobby.item.spawn.name")
            )) { event ->
                if (event.player.hasCooldown(Material.ENDER_PEARL))
                    return@rightClickItem

                event.player.setCooldown(Material.ENDER_PEARL, 20 * Main.config.getInt("config.lobby.tp-cooldown"))
                send(player)
            }
        )

        val show = showPlayers(player)

        player.inventory.setItem(
            4,
            Item.rightClickItem(ItemBuilder(
                if (show) Material.LIME_DYE
                else      Material.RED_DYE,
                player.language.child("gamelobby").getCmp("lobby.item.hide.${if (show) "shown" else "hidden"}")
            )) {
                if (player.hasCooldown(it.item!!.type))
                    return@rightClickItem

                showPlayers(player, !show)
                equipItems(player)

                player.setCooldown(Material.RED_DYE, 1 * 20)
                player.setCooldown(Material.LIME_DYE, 1 * 20)
            }
        )

        if (player.hasPermission(Permission.COMMAND_PRIVATE_GAME.string)) {
            player.inventory.setItem(
                5,
                Item.rightClickItem(ItemBuilder(
                    Material.NAME_TAG,
                    player.language.child("gamelobby").getCmp("lobby.item.private-game.name")
                )) { PrivateGame(player).open() }
            )
        }

        player.inventory.setItem(
            7,
            Item.rightClickItem(ItemBuilder(
                Material.FEATHER,
                player.language.child("gamelobby").getCmp("lobby.item.boost.name")
            )) { event ->
                if (event.player.hasCooldown(Material.FEATHER))
                    return@rightClickItem

                event.player.setCooldown(Material.FEATHER, 20 * Main.config.getInt("config.lobby.boost-cooldown"))
                event.player.velocity = event.player.location.direction.multiply(
                    Main.config.getDouble("config.lobby.boost-strength")
                )
            }
        )
    }

    val Player.isInLobby
        get() = this.world.name == spawn.world.name
}