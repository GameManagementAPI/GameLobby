package de.c4vxl.gamelobby

import de.c4vxl.gamemanager.language.Language
import de.c4vxl.gamemanager.utils.ResourceUtils
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.plugin.java.JavaPlugin

/**
 * Plugin entry point
 */
class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
    }

    override fun onLoad() {
        instance = this

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

        // Register language extensions
        ResourceUtils.readResource("langs", Main::class.java).split("\n")
            .forEach { langName ->
                Language.provideLanguageExtension(
                    "gamelobby",
                    langName,
                    ResourceUtils.readResource("lang/$langName.yml", Main::class.java)
                )
            }

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