/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.command.gui.commandContextMenu
import hextant.fx.*
import hextant.inspect.Inspections
import hextant.inspect.gui.InspectionPopup
import javafx.application.Platform
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.input.*
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCode.W

/**
 * An [EditorView] represented as a [javafx.scene.control.Control]
 * @param R the type of the root-[Node] of this control
 */
abstract class EditorControl<R : Node> : Control(), EditorView {
    private var _root: R? = null

    private lateinit var editor: Editor<*>

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
            activateSelection(editor)
            setRoot(newRoot)
        }

    override fun requestFocus() {
        root.requestFocus()
    }

    /**
     * Initialize this [EditorControl]
     * * Must be called exactly once after constructor logic, otherwise behaviour is undefined
     * @param editable the editable represented by this view
     * @param editor the editor represented by this view
     * @param context the [Context]
     */
    protected fun initialize(editable: Editable<*>, editor: Editor<*>, context: Context) {
        check(!initialized) { "already initialized" }
        this.editor = editor
        root = createDefaultRoot()
        activateContextMenu(editable, context)
        activateInspections(editable, context)
        activateSelectionExtension(editor)
        initialized = true
    }

    private fun activateSelectionExtension(editor: Editor<*>) {
        addEventHandler(KeyEvent.KEY_RELEASED) { k ->
            if (EXTEND_SELECTION.match(k) && editor.isSelected) {
                editor.parent?.extendSelection(editor)
                k.consume()
            } else if (SHRINK_SELECTION.match(k) && editor.isSelected) {
                editor.shrinkSelection()
            }
        }
    }


    private fun activateSelection(editor: Editor<*>) {
        root.focusedProperty().addListener { _, _, isFocused ->
            if (isFocused) {
                if (isControlDown) {
                    editor.toggleSelection()
                } else {
                    editor.select()
                }
            }
        }
    }


    private fun activateInspections(inspected: Any, context: Context) {
        val inspections = context[Inspections]
        val p = InspectionPopup(this) { inspections.getProblems(inspected) }
        registerShortcut(KeyCodeCombination(ENTER, KeyCombination.ALT_DOWN)) { p.show(this) }
    }

    private fun <T : Any> activateContextMenu(target: T, context: Context) {
        val contextMenu = target.commandContextMenu(context)
        setOnContextMenuRequested { contextMenu.show(this, Side.BOTTOM, 0.0, 0.0) }
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

    companion object {
        private val EXTEND_SELECTION = KeyCodeCombination(W, KeyCombination.SHORTCUT_DOWN)

        private val SHRINK_SELECTION = KeyCodeCombination(W, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
    }
}