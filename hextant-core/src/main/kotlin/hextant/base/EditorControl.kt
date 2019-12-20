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
import hextant.fx.ModifierValue.MAYBE
import hextant.impl.SelectionDistributor
import hextant.inspect.Inspections
import hextant.inspect.gui.InspectionPopup
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.KeyCode.*
import reaktive.value.*

/**
 * An [EditorView] represented as a [javafx.scene.control.Control]
 * @param R the type of the root-[Node] of this control
 * @property context the [Context] of this [EditorControl]
 */
abstract class EditorControl<R : Node>(
    final override val target: Any,
    val context: Context,
    arguments: Bundle
) : Control(), EditorView {
    constructor(editor: Editor<*>, arguments: Bundle) : this(editor, editor.context, arguments)

    private var _root: R? = null

    final override val arguments: Bundle

    override val group: EditorControlGroup = context[EditorControlGroup]

    private val selection = context[SelectionDistributor]

    private val inspections = context[Inspections]

    private val inspectionPopup = InspectionPopup(context) { inspections.getProblems(target) }

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

    private val editorChildren = mutableListOf<EditorControl<*>>()

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
        val reactive = Bundle.reactive(arguments)
        reactive.changed.subscribe { _, change ->
            argumentChanged(change.property, change.newValue)
        }
        this.arguments = reactive
        isFocusTraversable = false
        initShortcuts()
        //        activateContextMenu(target, context)
        sceneProperty().addListener { _, _, sc ->
            if (sc != null) handleProblem(hasError.now, hasWarning.now)
        }
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

    protected fun removeChild(idx: Int) {
        val prev = editorChildren.getOrNull(idx - 1)
        val next = editorChildren.getOrNull(idx + 1)
        prev?.next = next
        next?.previous = previous
        editorChildren.removeAt(idx)
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
            root.focusedProperty().addListener { _, _, focused ->
                if (focused && !manuallySelecting) {
                    if (isShiftDown) doToggleSelection()
                    else doSelect()
                }
            }
            setRoot(newRoot)
        }

    private var lastExtendingChild: EditorControl<*>? = null

    @Suppress("KDocMissingDocumentation")
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
            manuallySelecting = true
            root.requestFocus()
            manuallySelecting = false
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
            manuallySelecting = true
            root.requestFocus()
            manuallySelecting = false
        }
    }

    override fun deselect() {
        setSelected(false)
    }

    private fun setSelected(selected: Boolean) {
        _isSelected.set(selected)
        pseudoClassStateChanged(PseudoClasses.SELECTED, selected)
    }

    private fun initShortcuts() {
        registerShortcuts {
            on(EXTEND_SELECTION) { extendSelection() }
            on(SHRINK_SELECTION) { shrinkSelection() }
            maybeOn(INSPECTIONS) { showInspections() }
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

    private fun handleProblem(error: Boolean, warn: Boolean) {
        when {
            error -> {
                styleClass.add("error")
                styleClass.remove("warn")
            }
            warn  -> {
                styleClass.add("warn")
                styleClass.remove("error")
            }
            else  -> {
                styleClass.remove("warn")
                styleClass.remove("error")
            }
        }
    }

    private fun <T : Any> activateContextMenu(target: T, context: Context) {
        val contextMenu = target.commandContextMenu(context)
        setOnContextMenuRequested { contextMenu.show(this, Side.BOTTOM, 0.0, 0.0) }
    }

    companion object {
        private val EXTEND_SELECTION = shortcut(M) { control(MAYBE) }

        private val SHRINK_SELECTION = shortcut(L) { control(MAYBE) }

        private val INSPECTIONS = shortcut(ENTER) { alt(DOWN) }
    }
}