package de.c4vxl.gamelobby.commands

import de.c4vxl.gamelobby.GameLobby
import de.c4vxl.gamelobby.handler.SignHandler
import de.c4vxl.gamelobby.utils.ComponentCollection
import de.c4vxl.gamemanager.gamemanagementapi.GameManagementAPI
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.locationArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import java.util.*

object CreateSignCommand {
    init {
        val prefix = ComponentCollection.PREFIX.component
        val sorry = ComponentCollection.SORRY.component

        commandTree("createsign") {
            withFullDescription("Places a sign which when right clicked by a player will make him join a game with a fixed size")
            withPermission("c4vxl.gamelobby.perms.cmd.createsign")
            withAliases("cs")
            withUsage("/createsign <position> <gameSize>")

            locationArgument("location", LocationType.BLOCK_POSITION, true, optional = false) {
                argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings { _ ->
                    GameManagementAPI.possibleGames.toTypedArray()
                })) {
                    playerExecutor { player, args ->
                        if (!GameManagementAPI.possibleGames.contains(args.get("size").toString())) {
                            player.sendMessage(
                                prefix.append(sorry).append(
                                    Component.text("But this game size is not available!").color(NamedTextColor.WHITE)
                                )
                            )
                            return@playerExecutor
                        }

                        val location: Location = args.get("location") as? Location ?: player.location

                        if (SignHandler.placeSign(location, args.get("size").toString())) player.sendMessage(
                            GameLobby.prefix.append(
                                Component.text("Sign has been placed successfully!").color(
                                    NamedTextColor.GREEN)))
                        else
                            player.sendMessage(
                                prefix.append(sorry).append(
                                    Component.text("But this block is not a sign!").color(NamedTextColor.WHITE)
                                )
                            )
                    }
                }
            }
        }
    }
}