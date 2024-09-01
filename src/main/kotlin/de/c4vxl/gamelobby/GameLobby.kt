package de.c4vxl.gamelobby

import de.c4vxl.gamelobby.commands.CreateSignCommand
import de.c4vxl.gamelobby.commands.SetSpawnCommand
import de.c4vxl.gamelobby.handler.GameConnectionHandler
import de.c4vxl.gamelobby.handler.LobbyPlayerHandler
import de.c4vxl.gamelobby.handler.PlayerConnectionHandler
import de.c4vxl.gamelobby.handler.SignHandler
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.file.YamlConstructor
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class GameLobby : JavaPlugin() {
    companion object {
        val prefix: Component = Component.text("[").color(NamedTextColor.GRAY)
            .append(Component.text("Lobby").color(NamedTextColor.GREEN))
            .append(Component.text("] ").color(NamedTextColor.GRAY))

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
        
        // register commands
        CommandAPI.onEnable()
        SetSpawnCommand
        CreateSignCommand

        // register listeners
        GameConnectionHandler(this)
        LobbyPlayerHandler(this)
        PlayerConnectionHandler(this)
        SignHandler(this)

        logger.info("[+] $name has been enabled! \n  -> using version ${pluginMeta.version}")
    }

    override fun onDisable() {
        CommandAPI.onDisable()

        logger.info("[-] $name has been disabled!")
    }
}