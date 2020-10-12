/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.Property
import hextant.command.Command.Type.SingleReceiver
import hextant.command.meta.ProvideCommand
import hextant.context.*
import hextant.core.Editor
import hextant.core.EditorView
import hextant.core.editor.copyToClipboard
import hextant.core.editor.pasteFromClipboard
import hextant.fx.*
import hextant.impl.addListener
import hextant.impl.observe
import hextant.inspect.Inspections
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import reaktive.Observer
import reaktive.value.*
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmName

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

    private val changedArguments = mutableMapOf<Property<*, *, *>, Any?>()
    private val propertyChangeHandlers = context[Internal, Properties.propertyChangeHandlers]
    private val propertyObserver: Observer

    /**
     * Return a list of child [EditorControl]'s
     */
    fun editorChildren(): List<EditorControl<*>> = editorChildren

    internal var next: EditorControl<*>? = null
        private set

    internal fun next(): EditorControl<*>? = next ?: editorParent?.next()

    internal var previous: EditorControl<*>? = null
        private set

    internal fun previous(): EditorControl<*>? = previous ?: editorParent?.previous()

    private var manuallySelecting = false

    private val _isSelected = reactiveVariable(false)

    /**
     * A [ReactiveValue] holding `true` only if this [EditorControl] is selected at the moment
     */
    val isSelected: ReactiveValue<Boolean> get() = _isSelected

    init {
        styleClass.add("editor-control")
        for ((p, v) in arguments.entries) {
            propertyChangeHandlers.handle(this, p, v)
        }
        propertyObserver = arguments.changed.observe(this) { _, change ->
            changedArguments[change.property] = change.newValue
            propertyChangeHandlers.handle(this, change.property, change.newValue)
            argumentChanged(change.property, change.newValue)
        }
        sceneProperty().addListener(this) { sc ->
            if (sc != null) handleProblem(hasError.now, hasWarning.now)
        }
        isFocusTraversable = false
        initShortcuts()
    }

    /**
     * Is called when one of the display [arguments] changed.
     */
    open fun argumentChanged(property: Property<*, *, *>, value: Any?) {}

    internal open fun setEditorParent(parent: EditorControl<*>?) {
        editorParent = parent
    }

    internal open fun setNext(nxt: EditorControl<*>?) {
        next = nxt
    }

    internal open fun setPrevious(prev: EditorControl<*>?) {
        previous = prev
    }

    /**
     * Defines the list of children of this [EditorControl].
     * For all children their parent is set to this [EditorControl].
     * The left and right [EditorControl]s of the children are set according to their order in the list.
     */
    protected fun setChildren(children: List<EditorControl<*>>) {
        editorChildren.clear()
        if (children.isEmpty()) return
        editorChildren.addAll(children)
        children.forEach {
            it.root
            it.setEditorParent(this)
        }
        children.zipWithNext { previous, next ->
            previous.setNext(next)
            next.setPrevious(previous)
        }
        children.first().setPrevious(null)
        children.last().setNext(null)
    }

    /**
     * Make the given [EditorControl] a child of this editor control.
     */
    protected fun addChild(child: EditorControl<*>, idx: Int) {
        val prev = editorChildren.getOrNull(idx - 1)
        val next = editorChildren.getOrNull(idx)
        prev?.setNext(child)
        next?.setPrevious(child)
        child.setNext(next)
        child.setPrevious(prev)
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
        prev?.setNext(next)
        next?.setPrevious(prev)
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
            handleCommands(receiver, context)
            handleCommands(receiver.target, context)
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
        defaultShortcut = "Ctrl?+Shift?+V"
    )
    private fun paste(): Boolean = if (target is Editor<*>) target.pasteFromClipboard() else false

    @ProvideCommand(
        name = "Show Inspections",
        shortName = "inspect",
        defaultShortcut = "Alt+Enter",
        description = "Shows the inspection popup",
        type = SingleReceiver
    )
    private fun showInspections(): Boolean {
        inspectionPopup.show(root)
        return inspectionPopup.isShowing
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

    override fun createSnapshot(): ViewSnapshot<*> = Snapshot(this)

    private class Snapshot<C : EditorControl<*>>(original: C) : ViewSnapshot<C> {
        private val className = original::class.jvmName
        private val changedArguments = original.changedArguments
        private val children = original.editorChildren().map { it.snapshot() }

        override fun reconstruct(target: Any, arguments: Bundle): C {
            val cls = Class.forName(className).kotlin
            val constructor = cls.primaryConstructor ?: error("$cls has no primary constructor")
            val targetParameter = constructor.parameters[0]
            val argumentsParameter = constructor.parameters[1]
            val instance = constructor.callBy(mapOf(targetParameter to target, argumentsParameter to arguments))
            @Suppress("UNCHECKED_CAST")
            reconstruct(instance as C)
            return instance
        }

        override fun reconstruct(view: C) {
            check(view::class.jvmName == className) {
                "instance of ${view::class} passed is not valid for snapshot of class $className"
            }
            for ((p, v) in changedArguments) {
                @Suppress("UNCHECKED_CAST")
                p as Property<Any?, Any, Any>
                view.arguments[p] = v
            }
            for ((child, snapshot) in view.editorChildren().zip(children)) {
                snapshot.reconstruct(child)
            }
        }
    }
}
