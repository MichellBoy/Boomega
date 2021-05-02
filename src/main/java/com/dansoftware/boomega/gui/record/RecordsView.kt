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

package com.dansoftware.boomega.gui.record

import com.dansoftware.boomega.config.PreferenceKey
import com.dansoftware.boomega.config.Preferences
import com.dansoftware.boomega.db.Database
import com.dansoftware.boomega.db.data.Record
import com.dansoftware.boomega.gui.context.Context
import com.dansoftware.boomega.gui.keybinding.KeyBindings
import com.dansoftware.boomega.gui.record.dock.Dock
import com.dansoftware.boomega.i18n.I18N
import com.dansoftware.boomega.util.concurrent.CachedExecutor
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.IntegerBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.concurrent.WorkerStateEvent
import javafx.scene.layout.BorderPane
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.*

class RecordsView(
    private val context: Context,
    private val database: Database,
    private val preferences: Preferences
) : BorderPane() {

    private val copyHandle = Any()
    private val baseItems: ObservableList<Record> = FXCollections.observableArrayList()

    private val recordsViewBase = RecordsViewBase(context, database, baseItems)
    private val toolbar = RecordsViewToolbar(this)

    val table: RecordTable
        get() = recordsViewBase.table
    val docks: ObservableList<Dock>
        get() = recordsViewBase.docks

    var isFindDialogVisible: Boolean
        get() = recordsViewBase.isFindDialogVisible
        set(value) {
            recordsViewBase.isFindDialogVisible = value
        }

    init {
        buildUI()
        init()
    }

    private fun buildUI() {
        top = toolbar
        center = recordsViewBase
    }

    private fun init() {
        initFindKeyDetection()
        buildTableRowContextMenu()
        readConfigurations()
        loadRecords()
    }

    private fun initFindKeyDetection() {
        context.addKeyBindingDetection(KeyBindings.findRecordKeyBinding) {
            isFindDialogVisible = true
        }
    }

    private fun buildTableRowContextMenu() {
        RecordContextMenu(this).applyOn(table)
    }

    private fun readConfigurations() {
        readColumnConfigurations()
        readSortAbcConfigurations()
        readDockConfigurations()
    }

    private fun readColumnConfigurations() {
        recordsViewBase.columnsInfo = preferences.get(colConfigKey)
        toolbar.updateColumnChooser()
    }

    private fun readSortAbcConfigurations() {
        toolbar.abcLocale = preferences.get(abcConfigKey)
    }

    private fun readDockConfigurations() {
        recordsViewBase.dockInfo = preferences.get(docksConfigKey)
    }

    fun writeConfig() {
        preferences.editor()
            .put(colConfigKey, recordsViewBase.columnsInfo)
            .put(abcConfigKey, toolbar.abcLocale)
            .put(docksConfigKey, recordsViewBase.dockInfo)
    }

    fun refresh() {
        loadRecords()
    }

    private fun loadRecords() {
        CachedExecutor.submit(buildRecordsLoadTask())
    }

    private fun buildRecordsLoadTask() =
        TableRecordsGetTask(
            context,
            table,
            database
        ).apply {
            addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED) {
                baseItems.setAll(this.value)
            }
        }

    fun clipboardEmptyProperty(): BooleanBinding = RecordClipboard.emptyProperty()
        .or(RecordClipboard.identifierProperty().isEqualTo(copyHandle))

    fun itemsCountProperty(): IntegerBinding = Bindings.size(baseItems)

    fun scrollToTop() {
        table.scrollTo(0)
    }

    fun setItems(items: List<Record?>) {
        table.items.setAll(items)
    }

    fun cutSelectedToClipboard() {
        cutItemsToClipboard(ArrayList(table.selectionModel.selectedItems))
    }

    private fun cutItemsToClipboard(items: List<Record>) {
        RecordClipboard.pushItems(copyHandle, RecordClipboard.Action.CUT, items).also { push ->
            push.onPulled { removeItems(it.items) }
        }
    }

    fun copySelectedToClipboard() {
        copyItemsToClipboard(ArrayList(table.selectionModel.selectedItems))
    }

    private fun copyItemsToClipboard(items: List<Record>) {
        RecordClipboard.pushItems(copyHandle, RecordClipboard.Action.COPY, items)
    }

    fun pasteItemsFromClipboard() {
        CachedExecutor.submit(buildPasteAction(RecordClipboard.pullContent().items))
    }

    private fun buildPasteAction(items: List<Record>) =
        object : Task<Unit>() {
            init {
                setOnFailed { e ->
                    //TODO: error dialog
                    context.stopProgress()
                    logger.error("Couldn't paste record elements", e.source.exception)
                }
                setOnRunning { context.showIndeterminateProgress() }
                setOnSucceeded {
                    context.stopProgress()
                    refresh()
                    table.items.addAll(items)
                }
            }

            override fun call() {
                synchronized(RecordClipboard) {
                    logger.debug("Performing paste action....")
                    items.stream()
                        .map(Record::copy)
                        .peek { it.id = null }
                        .forEach(database::insertRecord)
                }
            }
        }

    fun removeSelectedItems() {
        //TODO: showing confirmation dialog
        removeItems(ArrayList(table.selectionModel.selectedItems))
    }

    fun insertNewRecord(record: Record = Record.Builder(Record.Type.BOOK).build()) {
        CachedExecutor.submit(buildInsertAction(record))
    }

    private fun buildInsertAction(record: Record): Task<Unit> =
        object : Task<Unit>() {

            init {
                setOnRunning {
                    context.showIndeterminateProgress()
                }

                setOnFailed {
                    context.stopProgress()
                    context.showErrorDialog(
                        I18N.getValue("record.add.error.title"),
                        I18N.getValue("record.add.error.msg"),
                        it.source.exception as Exception?
                    )
                }

                setOnSucceeded {
                    context.stopProgress()
                    baseItems.add(record)
                    table.selectionModel.clearSelection()
                    table.selectionModel.select(record)
                    table.scrollTo(record)
                }
            }

            override fun call() {
                database.insertRecord(record)
            }
        }

    private fun removeItems(items: List<Record>) {
        CachedExecutor.submit(buildRemoveAction(items))
    }

    private fun buildRemoveAction(items: List<Record>): Task<Unit> =
        object : Task<Unit>() {
            init {
                setOnRunning { context.showIndeterminateProgress() }
                setOnSucceeded {
                    context.stopProgress()
                    baseItems.removeAll(items)
                }
                setOnFailed { context.stopProgress() }
            }

            override fun call() {
                synchronized(RecordClipboard) {
                    logger.debug("Performing remove action...")
                    items.forEach(database::removeRecord)
                }
            }
        }

    companion object {
        private val logger = LoggerFactory.getLogger(RecordsView::class.java)

        private val colConfigKey =
            PreferenceKey(
                "books.view.table.columns",
                RecordsViewBase.TableColumnsInfo::class.java,
                RecordsViewBase.TableColumnsInfo.Companion::byDefault
            )

        private val docksConfigKey =
            PreferenceKey(
                "books.view.dock.info",
                RecordsViewBase.DockInfo::class.java,
                RecordsViewBase.DockInfo.Companion::defaultInfo
            )

        private val abcConfigKey =
            PreferenceKey(
                "books.view.module.table.abcsort",
                Locale::class.java,
                Locale::getDefault
            )
    }
}