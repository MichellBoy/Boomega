package com.dansoftware.libraryapp.gui.info.dependency

import com.dansoftware.libraryapp.gui.util.LibraryAppStage
import javafx.stage.Modality
import javafx.stage.Window

/**
 * A [Window] that is used for showing a [DependencyTable].
 *
 * @author Daniel Gyorffy
 */
class DependenciesWindow(dependencyTable: DependencyTable, owner: Window?) : LibraryAppStage("window.dependencies.title", dependencyTable) {
    init {
        initOwner(owner)
        initModality(Modality.APPLICATION_MODAL)
        isResizable = false
    }
}