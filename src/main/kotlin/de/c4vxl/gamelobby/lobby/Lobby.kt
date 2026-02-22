package de.c4vxl.gamelobby.lobby

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamelobby.utils.Item
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.utils.ItemBuilder
import org.bukkit.*
import org.bukkit.entity.Player

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
            Main.instance.saveConfig()
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