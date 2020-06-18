/**
 *@author Nikolaus Knop
 */

package hextant.fx

import bundles.Bundle
import bundles.Property
import hextant.command.Command.Type.SingleReceiver
import hextant.command.Commands
import hextant.command.line.CommandLine
import hextant.command.meta.ProvideCommand
import hextant.context.*
import hextant.core.*
import hextant.impl.addListener
import hextant.impl.observe
import hextant.inspect.Inspections
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
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

    private val inspectionPopup = InspectionPopup(context, target)
    internal val commandsPopup = CommandsPopup(context, target)

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
            handleCommands(this@EditorControl)
            handleCommands(target)
        }
    }

    @ProvideCommand(
        name = "Copy",
        shortName = "copy",
        type = SingleReceiver,
        description = "Copy the editor to the clipboard",
        defaultShortcut = "Ctrl?+C"
    )
    private fun copy(): Boolean = if (target is Editor<*>) target.copyToClipboard() else false

    @ProvideCommand(
        name = "Paste",
        shortName = "paste",
        type = SingleReceiver,
        description = "Paste the recently copied content",
        defaultShortcut = "Ctrl?+V"
    )
    private fun paste(): Boolean = if (target is Editor<*>) target.pasteFromClipboard() else false

    @ProvideCommand(
        name = "Show Inspections",
        defaultShortcut = "Alt+Enter",
        description = "Shows the inspection popup",
        type = SingleReceiver
    )
    private fun showInspections(): Boolean {
        inspectionPopup.show(this)
        return inspectionPopup.isShowing
    }

    private fun KeyEventHandlerBody<EditorControl<R>>.handleCommands(target: Any) {
        for (command in context[Commands].applicableOn(target)) {
            val shortcut = command.shortcut
            if (shortcut != null) {
                on(shortcut, consume = false) { ev ->
                    val cl = context[CommandLine]
                    cl.expand(command)
                    if (command.parameters.isEmpty()) {
                        val result = cl.execute()
                        if (result != null && result != false) ev.consume()
                    }
                }
            }
        }
    }

    @ProvideCommand(
        name = "Shrink Selection",
        shortName = "shrink",
        description = "Focuses the last focused child editor",
        defaultShortcut = "Ctrl+L",
        type = SingleReceiver
    )
    private fun shrinkSelection() {
        val childToSelect = lastExtendingChild ?: editorChildren.firstOrNull() ?: return
        childToSelect.requestFocus()
        if (isSelected.now) toggleSelection()
    }

    @ProvideCommand(
        name = "Extend Selection",
        shortName = "extend",
        description = "Focuses the parent editor",
        defaultShortcut = "Ctrl+M",
        type = SingleReceiver
    )
    private fun extendSelection() {
        val parent = editorParent ?: return
        parent.requestFocus()
        if (isSelected.now) toggleSelection()
        parent.lastExtendingChild = this
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
}
