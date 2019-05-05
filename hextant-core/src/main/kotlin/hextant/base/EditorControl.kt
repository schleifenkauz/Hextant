/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.bundle.*
import hextant.command.gui.commandContextMenu
import hextant.fx.*
import hextant.impl.SelectionDistributor
import hextant.inspect.Inspections
import hextant.inspect.gui.InspectionPopup
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.KeyCode.*
import reaktive.value.now
import reaktive.value.observe

/**
 * An [EditorView] represented as a [javafx.scene.control.Control]
 * @param R the type of the root-[Node] of this control
 */
abstract class EditorControl<R : Node>(
    final override val target: Any,
    val context: Context,
    arguments: Bundle
) : Control(), EditorView {
    private var _root: R? = null

    final override val arguments: Bundle

    private val selection = context[SelectionDistributor]

    private val inspections = context[Inspections]

    private val inspectionPopup = InspectionPopup { inspections.getProblems(target) }

    private val hasError = inspections.hasError(target)

    private val hasWarning = inspections.hasWarning(target)

    private val errorObserver = hasError.observe { _, isError ->
        handleProblem(isError, hasWarning.now)
    }

    private val warningObserver = hasWarning.observe { _, isWarn ->
        handleProblem(hasError.now, isWarn)
    }

    internal var editorParent: EditorControl<*>? = null
        private set

    private var editorChildren: List<EditorControl<*>>? = null

    internal var next: EditorControl<*>? = null
        private set

    internal var previous: EditorControl<*>? = null
        private set

    private var manuallySelecting = false

    init {
        val reactive = ReactiveBundle(arguments)
        reactive.changed.subscribe { _, change ->
            argumentChanged(change.property, change.newValue)
        }
        this.arguments = reactive
        isFocusTraversable = false
        initShortcuts()
        activateContextMenu(target, context)
        sceneProperty().addListener { _, _, sc ->
            if (sc != null) handleProblem(hasError.now, hasWarning.now)
        }
    }

    /**
     * Is called when one of the display arguments changed
     */
    protected open fun argumentChanged(property: Property<*, *, *>, value: Any?) {}

    internal open fun setEditorParent(parent: EditorControl<*>) {
        editorParent = parent
    }

    internal open fun setNext(nxt: EditorControl<*>) {
        next = nxt
    }

    internal open fun setPrevious(prev: EditorControl<*>) {
        previous = prev
    }

    fun focusNext() {
        next?.focus()
    }

    fun focusPrevious() {
        previous?.focus()
    }

    protected fun defineChildren(children: List<EditorControl<*>>) {
        editorChildren = children
        if (children.isEmpty()) return
        children.forEach {
            it.setEditorParent(this)
        }
        children.zipWithNext { previous, next ->
            previous.setNext(next)
            next.setPrevious(previous)
        }
        children.last().setNext(children.first())
        children.first().setPrevious(children.last())
    }

    protected fun defineChildren(vararg children: EditorControl<*>) {
        defineChildren(children.asList())
    }

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
        get() = _root ?: createDefaultRoot().also { root = it }
        protected set(newRoot) {
            _root = newRoot
            root.isFocusTraversable = true
            root.focusedProperty().addListener { _, _, focused ->
                if (focused && !manuallySelecting) {
                    if (isControlDown) doToggleSelection()
                    else doSelect()
                }
            }
            setRoot(newRoot)
        }

    private var lastExtendingChild: EditorControl<*>? = null

    override fun createDefaultSkin(): Skin<*> {
        root = createDefaultRoot()
        return skin
    }

    open fun focus() {
        log("focus")
        check(scene != null)
        root.requestFocus()
    }

    private fun log(msg: String) {
        //        println("${javaClass.name} $msg")
    }

    /**
     * Is called when this control should receive focus.
     * This method can delegate the focus to some child node as well.
     * The default implementation just calls [focus].
     */
    open fun receiveFocus() {
        log("receiveFocus")
        focus()
    }

    override fun requestFocus() {
        log("requestFocus")
        root.requestFocus()
    }

    private fun doSelect(): Boolean {
        log("doSelect")
        val selected = selection.select(this)
        pseudoClassStateChanged(PseudoClasses.SELECTED, selected)
        return selected
    }

    fun select() {
        log("select")
        if (doSelect()) {
            manuallySelecting = true
            root.requestFocus()
            manuallySelecting = false
        }
    }

    private fun doToggleSelection(): Boolean {
        log("doToggleSelection")
        val selected = selection.toggleSelection(this)
        pseudoClassStateChanged(PseudoClasses.SELECTED, selected)
        return selected
    }

    fun toggleSelection() {
        log("toggleSelection")
        if (doToggleSelection()) {
            manuallySelecting = true
            root.requestFocus()
            manuallySelecting = false
        }
    }

    override fun deselect() {
        log("deselect")
        pseudoClassStateChanged(PseudoClasses.SELECTED, false)
    }

    private fun initShortcuts() {
        registerShortcuts {
            on(EXTEND_SELECTION) { extendSelection() }
            on(SHRINK_SELECTION) { shrinkSelection() }
            maybeOn(INSPECTIONS) { showInspections() }
        }
    }

    private fun shrinkSelection() {
        val childToSelect = lastExtendingChild ?: editorChildren?.firstOrNull() ?: return
        childToSelect.select()
    }

    private fun extendSelection() {
        val parent = editorParent ?: return
        parent.select()
        parent.lastExtendingChild = this
    }

    private fun showInspections(): Boolean {
        inspectionPopup.show(this)
        return inspectionPopup.isShowing
    }

    private fun handleProblem(error: Boolean, warn: Boolean) {
        when {
            error -> root.setStyleForAllChildren("-fx-text-fill: red;")
            warn  -> root.setStyleForAllChildren("-fx-text-fill: yellow;")
            else  -> root.setStyleForAllChildren(null)
        }
    }

    private fun <T : Any> activateContextMenu(target: T, context: Context) {
        val contextMenu = target.commandContextMenu(context)
        setOnContextMenuRequested { contextMenu.show(this, Side.BOTTOM, 0.0, 0.0) }
    }

    companion object {
        private val EXTEND_SELECTION = Shortcut(W, CONTROL)

        private val SHRINK_SELECTION = Shortcut(W, CONTROL, SHIFT)

        private val INSPECTIONS = Shortcut(ENTER, ALT)

        private fun Node.setStyleForAllChildren(style: String?) {
            this.style = style
            if (this is Parent) {
                for (c in childrenUnmodifiable) {
                    c.setStyleForAllChildren(style)
                }
            }
        }
    }
}