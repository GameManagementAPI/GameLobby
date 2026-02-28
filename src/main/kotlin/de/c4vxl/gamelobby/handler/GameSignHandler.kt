package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.gma.player.GMAPlayer.Companion.gma
import de.c4vxl.gamemanager.language.Language.Companion.language
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

/**
 * Handles the registration and clicking of game signs
 */
class GameSignHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    companion object {
        /**
         * Registers a game sign
         * @param location The location of the sign-block
         * @param gameSize The game size
         */
        fun registerSign(location: Location, gameSize: String) {
            val sign: Sign = location.block.state as? Sign ?: return

            fun get(line: Int) =
                MiniMessage.miniMessage().deserialize(
                    Main.config.getString("config.lobby.game-signs.line$line")
                        ?.replace("\$size", gameSize)
                        ?: ""
                )

            sign.line(0, get(0))
            sign.line(1, get(1))
            sign.line(2, get(2))
            sign.line(3, get(3))

            // Store game information
            sign.persistentDataContainer.set(
                NamespacedKey("gamelobby", "gamesign.size"),
                PersistentDataType.STRING,
                gameSize
            )

            sign.update(true, true)
        }
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        val size = item.itemMeta.persistentDataContainer.get(
            NamespacedKey("gamelobby", "gamesign.size"),
            PersistentDataType.STRING
        ) ?: return

        event.isCancelled = false

        val block = event.blockPlaced.state as? Sign ?: return

        val type = block.type
        val data = block.blockData

        // "Close" sign
        event.blockPlaced.type = Material.AIR

        Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
            event.blockPlaced.type = type
            event.blockPlaced.blockData = data

            // Register sign
            registerSign(event.blockPlaced.location, size)
            event.player.sendMessage(event.player.language.child("gamelobby").getCmp("handler.gamesign.success"))
        }, 5)
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK)
            return

        val block = (event.clickedBlock ?: return).state as? Sign ?: return
        val size = GameSize.fromString(block.persistentDataContainer.get(
            NamespacedKey("gamelobby", "gamesign.size"),
            PersistentDataType.STRING
        ) ?: return) ?: return

        event.isCancelled = true

        // Get or create a game with desired size
        val game = GMA.getOrCreate(size)

        // Stop game if no maps available
        if (game.worldManager.availableMaps.isEmpty())
            GMA.unregisterGame(game, true)

        // Join
        val success = event.player.gma.join(game, false)

        if (!success) {
            event.player.sendMessage(event.player.language.child("gamelobby").getCmp("handler.gamesign.failure.already"))
        } else {
            event.player.sendActionBar(event.player.language.child("gamelobby").getCmp("handler.gamesign.join.success", size.toString()))
        }
    }
}