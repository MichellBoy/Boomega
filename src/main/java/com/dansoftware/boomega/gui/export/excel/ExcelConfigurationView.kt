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

package com.dansoftware.boomega.gui.export.excel

import com.dansoftware.boomega.export.excel.ExcelExportConfiguration
import com.dansoftware.boomega.gui.control.FontNamePicker
import com.dansoftware.boomega.gui.export.control.BaseConfigurationView
import com.dansoftware.boomega.gui.util.*
import com.dansoftware.boomega.i18n.i18n
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import jfxtras.styles.jmetro.JMetroStyleClass

// TODO: specifying sheet name
// TODO: specifying empty cell place holder

class ExcelConfigurationView(
    private val onFinished: (ExcelExportConfiguration) -> Unit
) : BorderPane() {

    private val excelExportConfiguration = ExcelExportConfiguration()

    init {
        VBox.setVgrow(this, Priority.ALWAYS)
        styleClass.addAll(JMetroStyleClass.BACKGROUND, "excel-configuration-view")
        buildUI()
    }

    private fun buildUI() {
        center = TabView(excelExportConfiguration)
        bottom = ExecuteArea()
    }

    private inner class ExecuteArea : StackPane() {
        init {
            padding = Insets(0.0, 20.0, 20.0, 20.0)
            children.add(buildExecuteButton())
        }

        private fun buildExecuteButton() = Button().apply {
            maxWidth = Double.MAX_VALUE
            isDefaultButton = true
            text = i18n("record.export.execute")
            setOnAction {
                onFinished(excelExportConfiguration)
            }
        }
    }

    private class TabView(private val excelExportConfiguration: ExcelExportConfiguration) : TabPane() {

        init {
            styleClass.addAll(JMetroStyleClass.UNDERLINE_TAB_PANE)
            buildUI()
        }

        private fun buildUI() {
            // TODO: i18n
            tabs.add(tab("General", GeneralView(excelExportConfiguration)))
            tabs.add(tab("Styling", scrollPane(StyleView(excelExportConfiguration), fitToWidth = true)))
        }

        private fun tab(title: String, content: Node) = Tab(title, content).apply {
            isClosable = false
        }
    }

    private class GeneralView(
        config: ExcelExportConfiguration
    ) : BaseConfigurationView<ExcelExportConfiguration>(config) {
        init {
            styleClass.add("general-view")
        }
    }

    private class StyleView(private val config: ExcelExportConfiguration) : GridPane() {
        init {
            styleClass.add("style-view")
            padding = Insets(20.0)
            hgap = 10.0
            vgap = 10.0
            buildUI()
        }

        private fun buildUI() {
            // TODO: i18n
            addRow(Label("Header").styleClass("category-label").colspan(2).hgrow(Priority.ALWAYS))
            addRow(Separator().colspan(2).hgrow(Priority.ALWAYS))
            addRow(Label("Background color: "))
            addRow(BackgroundColorPicker(config.headerCellStyle))
            addRow(Label("Font: "))
            addRow(CellFontChooser(config.headerCellStyle))
            addRow(Label("Font color: "))
            addRow(FontColorPicker(config.headerCellStyle))
            addRow(Label("Regular rows").styleClass("category-label").colspan(2).hgrow(Priority.ALWAYS))
            addRow(Separator().colspan(2).hgrow(Priority.ALWAYS))
            addRow(Label("Background color: "))
            addRow(BackgroundColorPicker(config.regularCellStyle))
            addRow(Label("Font:"))
            addRow(CellFontChooser(config.regularCellStyle))
            addRow(Label("Font color: "))
            addRow(FontColorPicker(config.regularCellStyle))
        }
    }

    private class CellFontChooser(private val cellStyle: ExcelExportConfiguration.CellStyle) : HBox(5.0) {
        init {
            GridPane.setHgrow(this, Priority.ALWAYS)
            buildUI()
        }

        private fun buildUI() {
            children.add(buildFontPicker())
            children.add(buildBoldToggle())
            children.add(buildItalicToggle())
            children.add(buildStrikeThroughToggle())
        }

        private fun buildFontPicker() = FontNamePicker().apply {
            setHgrow(this, Priority.ALWAYS)
            maxWidth = Double.MAX_VALUE
            valueProperty().onValuePresent {
                cellStyle.fontName = it
            }
        }

        private fun buildBoldToggle() = ToggleButton().apply {
            graphic = icon("bold-icon")
            isSelected = cellStyle.isBold
            selectedProperty().onValuePresent { cellStyle.isBold = it }
        }

        private fun buildItalicToggle() = ToggleButton().apply {
            graphic = icon("italic-icon")
            isSelected = cellStyle.isItalic
            selectedProperty().onValuePresent { cellStyle.isItalic = it }
        }

        private fun buildStrikeThroughToggle() = ToggleButton().apply {
            graphic = icon("strikethrough-icon")
            isSelected = cellStyle.isStrikeout
            selectedProperty().onValuePresent { cellStyle.isStrikeout = it }
        }
    }

    private class FontColorPicker(cellStyle: ExcelExportConfiguration.CellStyle) :
        ColorPicker(cellStyle.fontColor?.toFXColor() ?: Color.BLACK) {
        init {
            GridPane.setHgrow(this, Priority.ALWAYS)
            GridPane.setColumnSpan(this, 2)
            maxWidth = Double.MAX_VALUE
            valueProperty().onValuePresent { cellStyle.fontColor = it.toAWTColor() }
        }
    }

    private class BackgroundColorPicker(cellStyle: ExcelExportConfiguration.CellStyle) :
        ColorPicker(cellStyle.backgroundColor?.toFXColor() ?: Color.TRANSPARENT) {
            init {
                GridPane.setHgrow(this, Priority.ALWAYS)
                GridPane.setColumnSpan(this, 2)
                maxWidth = Double.MAX_VALUE
                valueProperty().onValuePresent { cellStyle.backgroundColor = it.toAWTColor() }
            }
        }
}