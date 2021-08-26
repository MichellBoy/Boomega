/*
 * Boomega
 * Copyright (C)  2021  Daniel Gyoerffy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dansoftware.boomega.gui.theme

import com.dansoftware.boomega.plugin.Plugins
import com.dansoftware.boomega.plugin.api.ThemePlugin
import okhttp3.internal.toImmutableList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("SupportedThemes")

/**
 * The list of the available [Theme]s can be used by the app.
 * Includes built-in and plugin themes as well.
 */
object AvailableThemes : List<Theme> by LinkedList(
    loadBuiltInThemes() + loadThemesFromPlugins()
).toImmutableList()

private fun loadBuiltInThemes(): List<Theme> = listOf(
    LightTheme,
    DarkTheme,
    OsSynchronizedTheme
)

private fun loadThemesFromPlugins(): List<Theme> {
    logger.debug("Checking plugins for themes...")
    return Plugins.getInstance().of(ThemePlugin::class.java).asSequence()
        .map(ThemePlugin::theme)
        .distinctBy(Any::javaClass)
        .onEach { logger.debug("Found [PLUGIN] theme: '{}'", it::class.java) }
        .toList()
}