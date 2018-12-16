/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.Control
import org.nikok.hextant.*
import org.nikok.hextant.core.fx.*

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
        get() = _root ?: throw IllegalStateException("root not yet initialized")
        protected set(newRoot) {
            _root = newRoot
            skin = null
            skin = skin(this, newRoot)
        }

    /**
     * Initialize this [EditorControl]
     * * Must be called exactly once after constructor logic, otherwise behaviour is undefined
     * @param editable the editable represented by this view
     * @param editor the editor represented by this view
     * @param platform the [HextantPlatform]
     */
    protected fun initialize(editable: Editable<*>, editor: Editor<*>, platform: HextantPlatform) {
        check(!initialized) { "already initialized" }
        root = createDefaultRoot()
        activateContextMenu(editable, platform)
        activateInspections(editable, platform)
        activateSelectionExtension(editor)
        initialized = true
    }

    private var initialized = false

    override fun select(isSelected: Boolean) {
        pseudoClassStateChanged(PseudoClasses.SELECTED, isSelected)
    }

    override fun error(isError: Boolean) {
        pseudoClassStateChanged(PseudoClasses.ERROR, isError)
    }

    /**
     * [requestFocus]
     */
    override fun focus() {
        requestFocus()
    }

    /**
     * Run the specified [action] on the JavaFX Application Thread
     */
    override fun onGuiThread(action: () -> Unit) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(action)
        } else action()
    }
}