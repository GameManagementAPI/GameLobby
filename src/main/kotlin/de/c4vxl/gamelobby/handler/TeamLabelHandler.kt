package de.c4vxl.gamelobby.handler

import de.c4vxl.gamelobby.Main
import de.c4vxl.gamemanager.gma.event.game.GameWorldLoadedEvent
import de.c4vxl.gamemanager.gma.team.Team
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Overwrites team labels
 */
class TeamLabelHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onMapLoad(event: GameWorldLoadedEvent) {
        if (!Main.config.getBoolean("team-labels.overwrite-labels", false))
            return

        // Overwrite prefix
        event.map.metadata.apply {
            event.game.teamManager.teams.keys.forEach { id ->
                set(
                    "team.$id.prefix",
                    Main.config.getString("team-labels.$id", null)
                )
            }

            save(event.map.configPath.toFile())
        }
    }
}