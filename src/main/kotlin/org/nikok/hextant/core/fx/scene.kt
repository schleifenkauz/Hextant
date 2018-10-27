/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.fx

import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.input.KeyCode.CONTROL
import javafx.scene.input.KeyEvent
import javafx.stage.Popup
import org.nikok.hextant.core.impl.Stylesheets
import kotlin.concurrent.thread

internal var isControlDown = false; private set

internal fun scene(root: Parent): Scene {
    val scene = Scene(root)
    scene.addEventFilter(KeyEvent.KEY_PRESSED) {
        if (it.code == CONTROL) {
            isControlDown = true
            println("Control is down")
        }
    }
    scene.addEventFilter(KeyEvent.KEY_RELEASED) {
        if (it.code == CONTROL) {
            isControlDown = false
            println("Control is up")
        }
    }
    displayShortcuts(scene)
    Stylesheets.apply(scene.stylesheets)
    return scene
}

private fun displayShortcuts(scene: Scene) {
    val p = Popup()
    val shortcutDisplay = Label().apply {
        style = "-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 20;"
    }
    p.scene.root.style = "-fx-background-color: transparent;"
    p.content.add(shortcutDisplay)
    var currentHiderThread: Thread? = null
    scene.addEventFilter(KeyEvent.KEY_RELEASED) { e ->
        if (e.isShortcut()) {
            currentHiderThread?.stop()
            shortcutDisplay.text = e.getShortcutString()
            p.show(scene.root)
            currentHiderThread = thread(start = true) {
                Thread.sleep(2000)
                Platform.runLater(p::hide)
                currentHiderThread = null
            }
        }
    }
}

private fun KeyEvent.getShortcutString(): String = buildString {
    if (isControlDown) append("Ctrl + ")
    if (isAltDown) append("Alt + ")
    if (isShiftDown) append("Shift + ")
    if (isMetaDown) append("Meta + ")
    append(code)
}

private fun KeyEvent.isShortcut() = isAltDown || isControlDown || isShortcutDown || isMetaDown