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

package com.dansoftware.boomega.gui.preferences.pane

import com.dansoftware.boomega.config.Preferences
import com.dansoftware.boomega.i18n.I18N
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import javafx.scene.Node
import javafx.scene.control.Button
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AdvancedPane(preferences: Preferences) : PreferencesPane(preferences) {

    override val title: String = I18N.getValue("preferences.tab.advanced")
    override val graphic: Node = MaterialDesignIconView(MaterialDesignIcon.PALETTE_ADVANCED)

    override fun buildContent(): Content =
        object : Content() {
            init {
                buildItems()
            }

            private fun buildItems() {
                items.add(
                    PairControl(
                        I18N.getValue("preferences.advanced.gc"),
                        I18N.getValue("preferences.advanced.gc.desc"),
                        buildGCButton()
                    )
                )
            }


            private fun buildGCButton() = Button("System.gc()").apply {
                setOnAction {
                    System.gc()
                    logger.debug("Garbage collection request made.")
                }
            }

        }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(AdvancedPane::class.java)
    }
}