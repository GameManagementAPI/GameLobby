package de.c4vxl.gamelobby

import de.c4vxl.gamelobby.commands.SetSpawnCommand
import de.c4vxl.gamelobby.handler.ConnectionHandler
import de.c4vxl.gamelobby.handler.GameConnectionHandler
import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ResourceUtils
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

/**
 * Plugin entry point
 */
class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
        lateinit var config: FileConfiguration
    }

    override fun onLoad() {
        instance = this
        Main.config = this.config

        // CommandAPI
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .verboseOutput(false)
                .silentLogs(true)
        )
    }

    override fun onEnable() {
        // CommandAPI
        CommandAPI.onEnable()

        // Load config
        saveResource("config.yml", false)
        reloadConfig()

        // Register language extensions
        ResourceUtils.readResource("langs", Main::class.java).split("\n")
            .forEach { langName ->
                // Read language file
                var translations = ResourceUtils.readResource("lang/$langName.yml", Main::class.java)

                // Add prefix to translations
                translations += "\n\"prefix\": \"${config.getString("config.prefix")}\""

                // Register extension
                Language.provideLanguageExtension(
                    "gamelobby",
                    langName,
                    translations
                )
            }

        // Register commands
        SetSpawnCommand

        // Register handlers
        ConnectionHandler()
        GameConnectionHandler()

        // Logging
        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // CommandAPI
        CommandAPI.onDisable()

        // Logging
        logger.info("[+] $name has been disabled!")
    }
}