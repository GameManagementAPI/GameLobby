package de.c4vxl.gamelobby.system

import de.c4vxl.gamelobby.GameLobby
import de.c4vxl.gamelobby.utils.ComponentCollection
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer
import de.c4vxl.gamemanager.gamemanagementapi.player.GMAPlayer.Companion.asGamePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.io.File

object Lobby {
    fun GMAPlayer.sendToLobby() = Lobby.sendPlayer(this.bukkitPlayer)
    val GMAPlayer.isInLobby get() = spawnLocation.world == this.bukkitPlayer.world
    fun Player.sendToLobby() = this.asGamePlayer.sendToLobby()
    val Player.isInLobby get() = this.asGamePlayer.isInLobby

    var spawnLocation: Location = Bukkit.getWorlds().first().spawnLocation
        get() = (GameLobby.config.getLocation("spawn") ?: Bukkit.getWorlds().first().spawnLocation).apply {
            // apply game rules to world
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false)
            world.setGameRule(GameRule.FALL_DAMAGE, false)
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            world.setGameRule(GameRule.KEEP_INVENTORY, true)
            world.setGameRule(GameRule.MOB_GRIEFING, false)
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
            world.setGameRule(GameRule.DROWNING_DAMAGE, false)
            world.setGameRule(GameRule.DO_FIRE_TICK, false)
        }
        set(value) {
            GameLobby.config.set("spawn", value)
            GameLobby.config.save(File(GameLobby.instance.dataFolder, "config.yml"))
            field = value
        }
    val players: MutableList<Player> get() = spawnLocation.world.players.filter { it.isInLobby }.toMutableList()

    private fun sendPlayer(player: Player) {
        if (!player.asGamePlayer.isInGame) {
            // reset player
            player.isFlying = false
            player.exp = 0F
            player.totalExperience = 0
            player.level = 0
            player.inventory.clear()
            player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
            player.fireTicks = 0
            player.resetMaxHealth()
            player.health = player.maxHealth
            player.gameMode = GameMode.SURVIVAL

            // add inventory items
            player.inventory.setItem(1, ItemStack(Material.ENDER_PEARL).apply {
                this.editMeta {
                    it.displayName(Component.text("Teleport to spawn").color(NamedTextColor.GOLD).append(ComponentCollection.RIGHT_CLICK.component))
                    it.persistentDataContainer.set(NamespacedKey(GameLobby.instance, "lobbyitem.action"), PersistentDataType.STRING, "tp_to_spawn")
                }
            })
            player.inventory.setItem(7, ItemStack(Material.FEATHER).apply {
                this.editMeta {
                    it.displayName(Component.text("Boost").color(NamedTextColor.AQUA).append(ComponentCollection.RIGHT_CLICK.component))
                    it.persistentDataContainer.set(NamespacedKey(GameLobby.instance, "lobbyitem.action"), PersistentDataType.STRING, "boost")
                }
            })
        }

        // teleport to spawn
        player.teleport(spawnLocation)
    }

    fun broadcast(message: Component) {
        players.forEach {
            it.sendMessage(message)
        }
    }

    fun broadcast(message: String) {
        players.forEach {
            it.sendMessage(message)
        }
    }
}