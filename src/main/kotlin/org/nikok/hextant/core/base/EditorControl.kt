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

/**
 * An [EditorView] represented as a [javafx.scene.control.Control]
 * @param R the type of the root-[Node] of this control
 */
abstract class EditorControl<R : Node> : Control(), EditorView {
    private var _root: R? = null

    /**
     * Creates the default root for this control
     */
    protected abstract fun createDefaultRoot(): R

    /**
     * The current root of this control
     * * Initially it has the value of [createDefaultRoot]
     * * Setting it updates the look of this control
     */
    var root: R
        get() {
            val r = _root
            return if (r != null) r
            else {
                val defaultRoot = createDefaultRoot()
                root = defaultRoot
                defaultRoot
            }
        }
        protected set(newRoot) {
            _root = newRoot
            skin = null
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