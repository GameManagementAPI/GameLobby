package de.c4vxl.gamelobby.commands

import de.c4vxl.gamelobby.GameLobby
import de.c4vxl.gamelobby.system.Lobby
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.locationArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location

object SetSpawnCommand {
    init {
        commandTree("setspawn") {
            withFullDescription("Allows you to set the spawn location of the current lobby")
            withPermission("c4vxl.gamelobby.perms.cmd.setspawn")
            withAliases("ss")
            withUsage("/setspawn [position]")

            locationArgument("location", LocationType.BLOCK_POSITION, true, optional = true) {
                playerExecutor { player, args ->
                    val location: Location = args.get("location") as? Location ?: player.location

                    Lobby.spawnLocation = location

                    player.sendMessage(GameLobby.prefix.append(Component.text("Spawn has been set successfully!").color(NamedTextColor.GREEN)))
                }
            }
        }
    }
}