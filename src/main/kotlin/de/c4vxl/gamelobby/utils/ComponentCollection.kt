package de.c4vxl.gamelobby.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

enum class ComponentCollection(val component: Component) {
    RIGHT_CLICK(Component.text(" | Right click").color(NamedTextColor.GRAY)),
    LEFT_CLICK(Component.text(" | Left click").color(NamedTextColor.GRAY))
}