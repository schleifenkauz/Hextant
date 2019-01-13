/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.impl.Stylesheets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent

internal var isControlDown = false; private set

fun hextantScene(root: Parent): Scene {
    val scene = Scene(root)
    scene.initHextantScene()
    return scene
}

fun Scene.initHextantScene() {
    addEventFilter(KeyEvent.KEY_PRESSED) {
        if (it.code == CONTROL) {
            isControlDown = true
        }
    }
    addEventFilter(KeyEvent.KEY_RELEASED) {
        if (it.code == CONTROL) {
            isControlDown = false
        }
    }
    Stylesheets.apply(this)
}

fun lastShortcutLabel(scene: Scene): Label {
    val shortcutDisplay = Label().apply {
        style = "-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 20;"
    }
    scene.addEventFilter(KeyEvent.KEY_RELEASED) { e ->
        if (e.isShortcut() || e.code == ENTER || e.code == TAB) {
            shortcutDisplay.text = e.getShortcutString()
        }
    }
    return shortcutDisplay
}

private fun KeyEvent.getShortcutString(): String = buildString {
    if (isControlDown) append("Ctrl + ")
    if (isAltDown) append("Alt + ")
    if (isShiftDown) append("Shift + ")
    if (isMetaDown) append("Meta + ")
    append(code)
}

private fun KeyEvent.isShortcut() = isAltDown || isControlDown || isShortcutDown || isMetaDown