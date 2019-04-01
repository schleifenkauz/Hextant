/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.HextantPlatform
import hextant.get
import hextant.impl.SelectionDistributor
import hextant.impl.Stylesheets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.input.*
import javafx.scene.input.KeyCode.*

var isControlDown = false; private set

fun hextantScene(root: (HextantPlatform) -> Parent): Scene {
    val platform = HextantPlatform.configured()
    val scene = Scene(root(platform))
    scene.initHextantScene(platform)
    return scene
}

fun Scene.initHextantScene(platform: HextantPlatform) {
    initEventHandlers(platform)
    Stylesheets.apply(this)
}

private fun Scene.initEventHandlers(platform: HextantPlatform) {
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
    addEventFilter(KeyEvent.KEY_RELEASED) { k ->
        val selectedEditors = platform[SelectionDistributor].selectedEditors.now
        if (EXTEND_SELECTION.match(k)) {
            for (editor in selectedEditors.toSet()) { //.toSet() copy is needed, because of concurrent modification
                editor.parent?.extendSelection(editor)
            }
            k.consume()
        } else if (SHRINK_SELECTION.match(k)) {
            for (editor in selectedEditors.toSet()) { //.toSet() copy is needed, because of concurrent modification
                editor.shrinkSelection()
            }
            k.consume()
        }
    }
}

private val EXTEND_SELECTION = KeyCodeCombination(W, KeyCombination.SHORTCUT_DOWN)

private val SHRINK_SELECTION = KeyCodeCombination(W, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)


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