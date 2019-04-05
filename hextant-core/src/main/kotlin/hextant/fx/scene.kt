/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.Context
import hextant.HextantPlatform
import hextant.impl.Stylesheets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent

var isControlDown = false; internal set

fun hextantScene(root: (Context) -> Parent, createContext: (HextantPlatform) -> Context): Scene {
    val platform = HextantPlatform.configured()
    val context = createContext(platform)
    val scene = Scene(root(context))
    scene.initHextantScene(context)
    return scene
}

fun Scene.initHextantScene(context: Context) {
    initEventHandlers(context)
    Stylesheets.apply(this)
}

@Suppress("UNUSED_PARAMETER")
private fun Scene.initEventHandlers(context: Context) {
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