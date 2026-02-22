package de.c4vxl.gamelobby.commands

import de.c4vxl.gamemanager.gma.GMA
import de.c4vxl.gamemanager.gma.game.type.GameSize
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.language.Language.Companion.language
import de.c4vxl.gamemanager.plugin.enums.Permission
import de.c4vxl.gamemanager.utils.ItemBuilder
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.persistence.PersistentDataType
import kotlin.jvm.optionals.getOrNull

object GameSignCommand {
    val command = commandTree("gamesign") {
        withPermission("${Permission.COMMAND_PREFIX.string}.gamesign")
        withAliases("gs")
        withUsage("/gamesign <create> <size> [type]")
        withFullDescription(Language.default.child("gamelobby").get("command.gamesign.desc"))

        literalArgument("create") {
            argument(StringArgument("size").replaceSuggestions(ArgumentSuggestions.strings {
                GMA.possibleGameSizes.toTypedArray()
            })) {
                argument(StringArgument("type").replaceSuggestions(ArgumentSuggestions.strings {
                    arrayOf("OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK", "MANGROVE", "CHERRY", "PALE_OAK", "BAMBOO", "CRIMSON", "WARPED")
                }), optional = true) {
                    playerExecutor { player, args ->
                        val type = args.getOptional("type")?.getOrNull()?.toString() ?: "OAK"
                        val material = Material.valueOf("${type}_SIGN")
                        val language = player.language.child("gamelobby")

                        val size = GameSize.fromString(args.get("size").toString())

                        // Invalid format
                        if (size == null) {
                            player.sendMessage(language.getCmp("command.gamesign.failure.invalid_format"))
                            return@playerExecutor
                        }

                        // No maps
                        if (!GMA.possibleGameSizes.contains(size.toString())) {
                            player.sendMessage(language.getCmp("command.gamesign.failure.invalid_size"))
                            return@playerExecutor
                        }

                        // Create sign
                        player.give(
                            ItemBuilder(
                                material,
                                Component.text(size.toString()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                                enchantments = mutableMapOf( Enchantment.POWER to 1 )
                            ).build()
                                .apply {
                                    this.itemMeta = this.itemMeta!!.apply {
                                        this.persistentDataContainer.set(
                                            NamespacedKey("gamelobby", "gamesign.size"),
                                            PersistentDataType.STRING,
                                            size.toString()
                                        )
                                    }
                                }
                        )

                        player.sendMessage(language.getCmp("command.gamesign.success"))
                    }
                }
            }
        }
    }
}