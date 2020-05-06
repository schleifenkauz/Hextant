/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.bundle.Bundle
import hextant.bundle.Property
import hextant.command.gui.commandContextMenu
import hextant.fx.*
import hextant.fx.ModifierValue.DOWN
import hextant.impl.*
import hextant.inspect.Inspections
import hextant.inspect.gui.InspectionPopup
import hextant.undo.UndoManager
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.KeyCode.Z
import reaktive.value.*

/**
 * An [EditorView] represented as a [javafx.scene.control.Control]
 * @param R the type of the root-[Node] of this control
 * @property context the [Context] of this [EditorControl]
 */
abstract class EditorControl<R : Node>(
    final override val target: Any,
    val context: Context,
    final override val arguments: Bundle
) : Control(), EditorView {
    constructor(editor: Editor<*>, arguments: Bundle) : this(editor, editor.context, arguments)

    private var _root: R? = null

    override val group: EditorControlGroup = context[EditorControlGroup]

    private val selection = context[SelectionDistributor]

    private val inspections = context[Inspections]

    private val inspectionPopup = run {
        val inspections = this.inspections //Prevent capturing of this
        InspectionPopup(context) { inspections.getProblems(target) }
    }

    private val hasError = inspections.hasError(target)

    private val hasWarning = inspections.hasWarning(target)

    private val errorObserver = hasError.observe(this) { _, _, isError ->
        handleProblem(isError, hasWarning.now)
    }

    private val warningObserver = hasWarning.observe(this) { _, _, isWarn ->
        handleProblem(hasError.now, isWarn)
    }

    internal var editorParent: EditorControl<*>? = null
        private set

    private val editorChildren = mutableListOf<EditorControl<*>>()

    /**
     * Return a list of child [EditorControl]'s
     */
    fun editorChildren(): List<EditorControl<*>> = editorChildren

    internal var next: EditorControl<*>? = null
        private set
        get() = field ?: editorParent?.next

    internal var previous: EditorControl<*>? = null
        private set
        get() = field ?: editorParent?.previous

    private var manuallySelecting = false

    private val _isSelected = reactiveVariable(false)

    /**
     * A [ReactiveValue] holding `true` only if this [EditorControl] is selected at the moment
     */
    val isSelected: ReactiveValue<Boolean> get() = _isSelected

    init {
        styleClass.add("editor-control")
        arguments.changed.observe(this) { _, change ->
            argumentChanged(change.property, change.newValue)
        }
        sceneProperty().addListener(this) { sc ->
            if (sc != null) handleProblem(hasError.now, hasWarning.now)
        }
        isFocusTraversable = false
        initShortcuts()
        //        activateContextMenu(target, context)
    }

    /**
     * Is called when one of the display arguments changed
     */
    open fun argumentChanged(property: Property<*, *, *>, value: Any?) {}

    internal open fun setEditorParent(parent: EditorControl<*>) {
        editorParent = parent
    }

    internal open fun setNext(nxt: EditorControl<*>) {
        next = nxt
    }

    internal open fun setPrevious(prev: EditorControl<*>) {
        previous = prev
    }

    /**
     * Focuses the [EditorControl] visually right or at the bottom of this one
     */
    fun focusNext() {
        next?.focus()
    }

    /**
     * Focuses the [EditorControl] visually left or at the top of this one
     */
    fun focusPrevious() {
        previous?.focus()
    }

    /**
     * Defines the list of children of this [EditorControl].
     * For all children their parent is set to this [EditorControl].
     * The left and right [EditorControl]s of the children are set according to their order in the list.
     */
    protected fun setChildren(children: List<EditorControl<*>>) {
        editorChildren.clear()
        editorChildren.addAll(children)
        if (children.isEmpty()) return
        children.forEach {
            it.root
            it.setEditorParent(this)
        }
        children.zipWithNext { previous, next ->
            previous.setNext(next)
            next.setPrevious(previous)
        }
    }

    /**
     * Make the given [EditorControl] a child of this editor control.
     */
    protected fun addChild(child: EditorControl<*>, idx: Int) {
        val prev = editorChildren.getOrNull(idx - 1)
        val next = editorChildren.getOrNull(idx)
        prev?.next = child
        next?.previous = child
        child.next = next
        child.previous = previous
        child.setEditorParent(this)
        editorChildren.add(idx, child)
    }

    private fun clearChildren() {
        editorChildren.clear()
    }

    /**
     * Remove the editor child at the given [index]
     */
    protected fun removeChild(index: Int) {
        val prev = editorChildren.getOrNull(index - 1)
        val next = editorChildren.getOrNull(index + 1)
        prev?.next = next
        next?.previous = previous
        editorChildren.removeAt(index)
    }

    /**
     * Delegates to [setChildren]
     */
    protected fun setChildren(vararg children: EditorControl<*>) {
        setChildren(children.asList())
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
            root.focusedProperty().addListener(this) { focused ->
                if (focused && !manuallySelecting) {
                    if (isShiftDown) doToggleSelection()
                    else doSelect()
                }
            }
            setRoot(newRoot)
        }

    private var lastExtendingChild: EditorControl<*>? = null

    /**
     * Uses [createDefaultRoot] to create a skin.
     */
    override fun createDefaultSkin(): Skin<*> {
        root = createDefaultRoot()
        return skin
    }

    override fun focus() {
        root.requestFocus()
    }

    /**
     * Is called when this control should receive focus.
     * This method can delegate the focus to some child node as well.
     * The default implementation just calls [focus].
     */
    open fun receiveFocus() {
        focus()
    }

    /**
     * Delegates to the [EditorControl.root]
     */
    override fun requestFocus() {
        root.requestFocus()
    }

    private fun doSelect(): Boolean {
        val selected = selection.select(this)
        setSelected(selected)
        return selected
    }

    /**
     * Select this editor control and request focus.
     */
    fun select() {
        if (doSelect()) {
            justFocus()
        }
    }

    private fun doToggleSelection(): Boolean {
        val selected = selection.toggleSelection(this)
        setSelected(selected)
        return selected
    }

    /**
     * Toggle the selection of this [EditorControl] and request focus if it is selected afterwards
     */
    fun toggleSelection() {
        if (doToggleSelection()) {
            justFocus()
        }
    }

    /**
     * Just focus this [EditorControl] without selecting.
     */
    fun justFocus() {
        manuallySelecting = true
        root.requestFocus()
        manuallySelecting = false
    }

    override fun deselect() {
        setSelected(false)
    }

    private fun setSelected(selected: Boolean) {
        _isSelected.set(selected)
        root.pseudoClassStateChanged(PseudoClasses.SELECTED, selected)
    }

    private fun initShortcuts() {
        registerShortcuts(this) {
            on(EXTEND_SELECTION) { extendSelection() }
            on(SHRINK_SELECTION) { shrinkSelection() }
            on(shortcut(Z) { control(DOWN) }, consume = false) { ev ->
                val manager = context[UndoManager]
                if (manager.canUndo) {
                    manager.undo()
                    ev.consume()
                }
            }
            on(shortcut(Z) { control(DOWN); shift(DOWN) }) { ev ->
                val manager = context[UndoManager]
                if (manager.canRedo) {
                    manager.redo()
                    ev.consume()
                }
            }
            on(INSPECTIONS, consume = false) { ev ->
                if (showInspections()) ev.consume()
            }
            on(COPY_VIM, consume = false) { ev ->
                if (target is Editor<*>) {
                    val success = target.copyToClipboard()
                    if (success) {
                        ev.consume()
                    }
                }
            }
            on(PASTE_VIM, consume = false) { ev ->
                if (target is Editor<*>) {
                    val success = target.pasteFromClipboard()
                    if (success) {
                        ev.consume()
                    }
                }
            }
        }
    }

    private fun shrinkSelection() {
        val childToSelect = lastExtendingChild ?: editorChildren.firstOrNull() ?: return
        childToSelect.requestFocus()
        if (isSelected.now) toggleSelection()
    }

    private fun extendSelection() {
        val parent = editorParent ?: return
        parent.requestFocus()
        if (isSelected.now) toggleSelection()
        parent.lastExtendingChild = this
    }

    private fun showInspections(): Boolean {
        inspectionPopup.show(this)
        return inspectionPopup.isShowing
    }

    private fun addStyleCls(name: String) {
        if (name !in styleClass) styleClass.add(name)
    }

    private fun handleProblem(error: Boolean, warn: Boolean) {
        when {
            error -> {
                addStyleCls("error")
                styleClass.remove("warning")
            }
            warn  -> {
                addStyleCls("warning")
                styleClass.remove("error")
            }
            else  -> {
                styleClass.remove("warning")
                styleClass.remove("error")
            }
        }
    }

    private fun <T : Any> activateContextMenu(target: T, context: Context) {
        val contextMenu = target.commandContextMenu(context)
        setOnContextMenuRequested { contextMenu.show(this, Side.BOTTOM, 0.0, 0.0) }
    }

    companion object {
        private const val EXTEND_SELECTION = "Ctrl?+M"

        private const val SHRINK_SELECTION = "Ctrl?+L"

        private const val INSPECTIONS = "Alt + Enter"

        private const val COPY = "Ctrl + Shift + C"

        private const val COPY_VIM = "C"

        private const val PASTE = "Ctrl + Shift + V"

        private const val PASTE_VIM = "V"
    }
}
