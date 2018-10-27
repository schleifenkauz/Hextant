/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.fx

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.input.KeyCode.SHIFT
import javafx.scene.input.KeyEvent
import org.nikok.hextant.core.impl.Stylesheets

internal var isShiftDown = false; private set

internal fun scene(root: Parent): Scene {
    val scene = Scene(root)
    scene.addEventFilter(KeyEvent.KEY_PRESSED) {
        if (it.code == SHIFT) {
            isShiftDown = true
            println("Shift is down")
        }
    }
    scene.addEventFilter(KeyEvent.KEY_RELEASED) {
        if (it.code == SHIFT) {
            isShiftDown = false
            println("Shift is up")
        }
    }
    Stylesheets.apply(scene.stylesheets)
    return scene
}