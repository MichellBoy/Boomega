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

package com.dansoftware.boomega.gui.menu

import com.dansoftware.boomega.config.Preferences
import com.dansoftware.boomega.database.tracking.DatabaseTracker
import com.dansoftware.boomega.gui.action.impl.OpenClipboardViewerAction
import com.dansoftware.boomega.gui.action.menuItemOf
import com.dansoftware.boomega.gui.api.Context
import com.dansoftware.boomega.gui.util.menuItem
import com.dansoftware.boomega.i18n.i18n
import javafx.scene.control.Menu

class ClipboardMenu(
    private val context: Context,
    private val preferences: Preferences,
    private val databaseTracker: DatabaseTracker
) : Menu(i18n("menubar.menu.clipboard")) {

    init {
        this.menuItem(clipboardViewItem())
    }

    private fun clipboardViewItem() =
        menuItemOf(OpenClipboardViewerAction, context, preferences, databaseTracker)
}