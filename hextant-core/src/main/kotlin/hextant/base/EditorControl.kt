/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.bundle.*
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
abstract class EditorControl<R : Node>(arguments: Bundle) : Control(), EditorView {
    private var _root: R? = null

    private lateinit var editor: Editor<*>

    final override val arguments: Bundle

    private var focusingAfterSelection = false

    private var isError = false

    private var isWarn = false

    init {
        val reactive = ReactiveBundle(arguments)
        reactive.changed.subscribe { _, change ->
            argumentChanged(change.property, change.newValue)
        }
        this.arguments = reactive
    }

    /**
     * Is called when one of the display arguments changed
     */
    protected open fun argumentChanged(property: Property<*, *, *>, value: Any?) {}

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
            root.isFocusTraversable = true
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


    private fun activateSelection(editor: Editor<*>) {
        root.focusedProperty().addListener { _, _, isFocused ->
            if (!focusingAfterSelection && isFocused) {
                if (isControlDown) {
                    editor.toggleSelection()
                } else {
                    editor.select()
                }
            }
        }
    }

    private fun activateSelectionExtension(editor: Editor<*>) {
        addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
            if (EXTEND_SELECTION.match(ev)) {
                editor.parent?.extendSelection(editor)
                ev.consume()
            } else if (SHRINK_SELECTION.match(ev)) {
                editor.shrinkSelection()
                ev.consume()
            }
        }
    }

    private fun activateInspections(inspected: Any, context: Context) {
        val inspections = context[Inspections]
        val p = InspectionPopup { inspections.getProblems(inspected) }
        addEventHandler(KeyEvent.KEY_RELEASED) { k ->
            if (KeyCodeCombination(ENTER, KeyCombination.ALT_DOWN).match(k)) {
                p.show(this)
                if (p.isShowing) {
                    k.consume()
                }
            }
        }
    }

    private fun <T : Any> activateContextMenu(target: T, context: Context) {
        val contextMenu = target.commandContextMenu(context)
        setOnContextMenuRequested { contextMenu.show(this, Side.BOTTOM, 0.0, 0.0) }
    }

    private var initialized = false

    override fun select(isSelected: Boolean) {
        pseudoClassStateChanged(PseudoClasses.SELECTED, isSelected)
        if (isSelected) {
            focusingAfterSelection = true
            focus()
            focusingAfterSelection = false
        }
    }

    override fun error(error: Boolean) {
        this.isError = error
        displayProblem()
    }

    override fun warn(warn: Boolean) {
        this.isWarn = warn
        displayProblem()
    }

    private fun displayProblem() {
        when {
            isError -> root.style = "-fx-text-fill: red;"
            isWarn  -> root.style = "-fx-text-fill: yellow;"
            else    -> root.style = null
        }
    }

    /**
     * [requestFocus]
     */
    final override fun focus() {
        onGuiThread { requestFocus() }
    }

    /**
     * Run the specified [action] on the JavaFX Application Thread
     */
    override fun onGuiThread(action: () -> Unit) =
        if (Platform.isFxApplicationThread()) action() else {
            Platform.runLater(action)
        }

    companion object {
        private val EXTEND_SELECTION = KeyCodeCombination(W, KeyCombination.SHORTCUT_DOWN)

        private val SHRINK_SELECTION = KeyCodeCombination(W, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
    }
}