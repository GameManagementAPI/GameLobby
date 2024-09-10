package de.c4vxl.gamelobby

import de.c4vxl.gamelobby.commands.CreateSignCommand
import de.c4vxl.gamelobby.commands.SetSpawnCommand
import de.c4vxl.gamelobby.handler.*
import de.c4vxl.gamelobby.utils.ItemBuilder
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class GameLobby : JavaPlugin() {
    companion object {
        lateinit var prefix: Component

        lateinit var instance: JavaPlugin

        lateinit var config: FileConfiguration
    }

    override fun onLoad() {
        instance = this

        CommandAPI.onLoad(CommandAPIBukkitConfig(this).silentLogs(true))
    }

    override fun onEnable() {
        // init config
        saveResource("config.yml", false)
        GameLobby.config = YamlConfiguration.loadConfiguration(File(dataFolder, "config.yml"))

        // init prefix from config
        prefix = LegacyComponentSerializer.legacySection().deserialize(GameLobby.config.getString("prefix") ?: "§r§7[§r§aLobby§r§7]§r ")

        // register ItemBuilder
        ItemBuilder.register(this)

        // register commands
        CommandAPI.onEnable()
        SetSpawnCommand
        CreateSignCommand

        // register listeners
        GameConnectionHandler(this)
        LobbyPlayerHandler(this)
        PlayerConnectionHandler(this)
        SignHandler(this)
        QueueHandler(this)
        GameFinishHandler(this)
        SpectatorHandler(this)

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        CommandAPI.onDisable()

        logger.info("[-] $name has been disabled!")
    }
}