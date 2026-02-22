package de.c4vxl.gamelobby.commands

import de.c4vxl.gamelobby.lobby.Lobby
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Location
import kotlin.jvm.optionals.getOrNull

/**
 * Command for setting the lobby spawn position
 */
object SetSpawnCommand {
    val command = commandTree("setspawn") {
        withPermission("${Permission.COMMAND_PREFIX.string}.setspawn")
        withAliases("ss")
        withUsage("/setspawn [position] [direction]")
        withFullDescription(Language.default.child("gamelobby").get("command.setspawn.desc"))

        locationArgument("location", LocationType.BLOCK_POSITION, optional = true) {
            rotationArgument("direction", optional = true) {
                playerExecutor { player, args ->
                    val location = args.getOptional("location").getOrNull()?.let { it as? Location } ?: player.location
                    val direction = args.getOptional("direction").getOrNull()?.toString() ?: "${player.location.yaw} ${player.location.pitch}"

                    // Set direction
                    direction.split(" ").let { parts ->
                        parts[0].toFloatOrNull()?.let { location.yaw = it }
                        parts[1].toFloatOrNull()?.let { location.pitch = it }
                    }

                    // Update position
                    Lobby.spawn = location

                    // Send success message
                    player.sendMessage(player.language.child("gamelobby").getCmp("command.setspawn.success"))
                }
            }
        }
    }
}