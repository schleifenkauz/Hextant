/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Control
import org.nikok.hextant.EditorView
import org.nikok.hextant.core.fx.PseudoClasses
import org.nikok.hextant.core.fx.skin

abstract class EditorControl<R : Node> : Control(), EditorView {
    private var _root: R? = null

    protected abstract fun createDefaultRoot(): R

    protected var root: R
        get() {
            val r = _root
            return if (r != null) r
            else {
                val defaultRoot = createDefaultRoot()
                root = defaultRoot
                defaultRoot
            }
        }
        set(newRoot) {
            _root = newRoot
            skin = skin(this, newRoot)
        }

    override fun select(isSelected: Boolean) {
        pseudoClassStateChanged(PseudoClasses.SELECTED, isSelected)
    }

    override fun error(isError: Boolean) {
        pseudoClassStateChanged(PseudoClasses.ERROR, isError)
    }

    override fun focus() {
        requestFocus()
    }

    override fun onGuiThread(action: () -> Unit) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(action)
        } else action()
    }
}