/**
 * Provides utilities in the subject of JavaFX
 *
 * @author Daniel Gyorffy
 */
@file:JvmName("BaseFXUtils")

package com.dansoftware.boomega.gui.util

import com.dansoftware.boomega.i18n.I18N
import com.dansoftware.boomega.util.equalsIgnoreCase
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.value.ObservableValueBase
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.stage.Window
import java.io.BufferedInputStream
import java.util.function.Consumer
import kotlin.reflect.KClass

fun runOnUiThread(action: Runnable) {
    when {
        Platform.isFxApplicationThread() -> action.run()
        else -> Platform.runLater(action)
    }
}

fun <T> ComboBox<T>.refresh() {
    val items: ObservableList<T> = this.items
    val selected: T = this.selectionModel.selectedItem
    this.items = null
    this.items = items
    this.selectionModel.select(selected)
}

fun findSelectedRadioItem(items: List<MenuItem>): MenuItem? =
    items.filterIsInstance<RadioMenuItem>()
        .find { it.isSelected }

fun <T> constantObservable(value: () -> T): ObservableValue<T> =
    object : ObservableValueBase<T>() {
        override fun getValue(): T = value()
    }

fun loadImage(resource: String, onImageReady: Consumer<Image>) {
    val image = Image(resource, true)
    image.progressProperty().addListener(object : ChangeListener<Number> {
        override fun changed(observable: ObservableValue<out Number>, oldValue: Number, newValue: Number) {
            if (newValue == 1.0 && image.isError.not()) {
                onImageReady.accept(image)
                observable.removeListener(this)
            }
        }
    })
}

fun Node.onWindowPresent(action: Consumer<Window>) {
    this.scene?.window?.also { action.accept(it) }
    this.sceneProperty().addListener(object : ChangeListener<Scene> {
        override fun changed(observable: ObservableValue<out Scene>, oldValue: Scene?, newValue: Scene?) {
            if (newValue != null) {
                scene.windowProperty().addListener(object : ChangeListener<Window> {
                    override fun changed(
                        observable: ObservableValue<out Window>,
                        oldValue: Window?,
                        newValue: Window?
                    ) {
                        if (newValue != null) {
                            action.accept(newValue)
                            observable.removeListener(this)
                        }
                    }

                })
                observable.removeListener(this)
            }
        }
    })

}

/**
 * Sets the action of the [MenuItem] and then returns the object itself
 */
fun MenuItem.action(onAction: EventHandler<ActionEvent>): MenuItem = this.also { this.onAction = onAction }

/**
 * Sets the key combination of the [MenuItem] and then returns the object itself
 */
fun MenuItem.keyCombination(combination: KeyCombination): MenuItem = this.also { it.accelerator = combination }

/**
 * Binds the key combination property of the [MenuItem] to the given property and then returns the object itself
 */
fun <T : KeyCombination> MenuItem.keyCombination(combination: ObjectProperty<T>) = this.apply { acceleratorProperty().bind(combination) }

/**
 * Sets the icon of the [MenuItem] and then returns the object itself
 */
fun MenuItem.graphic(icon: MaterialDesignIcon): MenuItem = this.also { it.graphic = MaterialDesignIconView(icon) }

/**
 * Adds a sub menu item into the [Menu] and then returns the object itself
 */
fun Menu.menuItem(item: MenuItem): Menu = this.also { this.items.add(item) }

/**
 * Adds a [SeparatorMenuItem] into the [Menu] and then returns the object itself
 */
fun Menu.separator(): Menu = this.also { this.items.add(SeparatorMenuItem()) }

/**
 * Utility function that converts a [KeyCodeCombination] into a [KeyEvent] object,
 * simulating that the particular key-combination is pressed by the user
 */
fun KeyCodeCombination.asKeyEvent(): KeyEvent =
    KeyEvent(
        KeyEvent.KEY_PRESSED,
        this.code.toString(),
        this.displayText,
        this.code,
        this.shift == KeyCombination.ModifierValue.DOWN,
        this.control == KeyCombination.ModifierValue.DOWN,
        this.alt == KeyCombination.ModifierValue.DOWN,
        this.meta == KeyCombination.ModifierValue.DOWN
    )

fun KeyEvent.asKeyCombination(): KeyCombination? =
    mutableListOf<KeyCombination.Modifier>().also { modifiers ->
        this.isControlDown.takeIf { it }?.let { modifiers.add(KeyCombination.CONTROL_DOWN) }
        this.isAltDown.takeIf { it }?.let { modifiers.add(KeyCombination.ALT_DOWN) }
        this.isShiftDown.takeIf { it }?.let { modifiers.add(KeyCombination.SHIFT_DOWN) }
        this.isMetaDown.takeIf { it }?.let { modifiers.add(KeyCombination.META_DOWN) }
        this.isShortcutDown.takeIf { it }?.let { modifiers.add(KeyCombination.SHORTCUT_DOWN) }
    }.let {
        this.code?.let { _ ->
            try {
                KeyCodeCombination(this.code, *it.toTypedArray())
            } catch(e: RuntimeException) {
                null
            }
        }
    }

fun KeyEvent.isOnlyCode(): Boolean {
    var count: Int = 0
    this.isControlDown.takeIf { it }?.let { count++ }
    this.isAltDown.takeIf { it }?.let { count++ }
    this.isShiftDown.takeIf { it }?.let { count++ }
    this.isMetaDown.takeIf { it }?.let { count++ }
    this.isShortcutDown.takeIf { it }?.let { count++ }
    return count == 0
}

fun KeyEvent.isUndefined(): Boolean =
    this.code.name.equalsIgnoreCase("undefined")


/**
 * Determines that a ButtonType's button data is the same.
 */
fun ButtonType.typeEquals(other: ButtonType) = this.buttonData == other.buttonData

/**
 * Loads a resource into a javaFX [Image]
 */
fun KClass<*>.loadImageResource(resource: String): Image {
    BufferedInputStream(this.java.getResourceAsStream(resource)).use {
        return Image(it)
    }
}

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

    @JvmField
    val RETRY = createButtonType("Dialog.retry.button", ButtonBar.ButtonData.YES)

    private fun createButtonType(key: String, buttonData: ButtonBar.ButtonData) =
        ButtonType(I18N.getValues().getString(key), buttonData)
}

