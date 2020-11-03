package com.dansoftware.libraryapp.gui.util

import com.dansoftware.libraryapp.locale.I18N
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType

/**
 * Provides internationalized [ButtonType] constants for the app.
 *
 * @author Daniel Gyorffy
 */
object I18NButtonTypes {
    @JvmField
    val APPLY = createButtonType("Dialog.apply.button", ButtonBar.ButtonData.APPLY)

    @JvmField
    val OK = createButtonType("Dialog.ok.button", ButtonBar.ButtonData.OK_DONE)

    @JvmField
    val CANCEL = createButtonType("Dialog.cancel.button", ButtonBar.ButtonData.CANCEL_CLOSE)

    @JvmField
    val CLOSE = createButtonType("Dialog.close.button", ButtonBar.ButtonData.CANCEL_CLOSE)

    @JvmField
    val YES = createButtonType("Dialog.yes.button", ButtonBar.ButtonData.YES)

    @JvmField
    val NO = createButtonType("Dialog.no.button", ButtonBar.ButtonData.NO)

    @JvmField
    val FINISH = createButtonType("Dialog.finish.button", ButtonBar.ButtonData.FINISH)

    @JvmField
    val NEXT = createButtonType("Dialog.next.button", ButtonBar.ButtonData.NEXT_FORWARD)

    @JvmField
    val PREVIOUS = createButtonType("Dialog.previous.button", ButtonBar.ButtonData.BACK_PREVIOUS)

    private fun createButtonType(key: String, buttonData: ButtonBar.ButtonData) = ButtonType(I18N.getButtonTypeValues().getString(key), buttonData)
}