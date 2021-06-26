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

package com.dansoftware.boomega.gui.pluginmngr

import com.dansoftware.boomega.plugin.PluginDirectory
import javafx.stage.Window
import java.io.File

/**
 * For showing a [PluginManager] with a [PluginManagerWindow].
 *
 * @author Daniel Gyorffy
 */
class PluginManagerActivity {

    fun show() = show(null)

    fun show(ownerWindow: Window?) {
        show(ownerWindow, PluginDirectory.getPluginFiles().asList())
    }

    fun show(ownerWindow: Window?, pluginFiles: List<File>?) {
        PluginManagerWindow(ownerWindow, PluginManager(pluginFiles ?: emptyList())).show()
    }
}