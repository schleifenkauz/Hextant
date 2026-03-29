/**
 *@author Nikolaus Knop
 */

@file:OptIn(InternalSerializationApi::class)
@file:Suppress("UNCHECKED_CAST")

package hextant.core.view

import bundles.Bundle
import bundles.Property
import bundles.Public
import fxutils.PseudoClasses
import fxutils.registerShortcuts
import fxutils.setRoot
import fxutils.show
import hextant.context.Context
import hextant.context.Properties
import hextant.context.SelectionDistributor
import hextant.core.Editor
import hextant.core.EditorView
import hextant.core.editor.copyToClipboard
import hextant.core.editor.pasteFromClipboard
import hextant.fx.InspectionPopup
import hextant.fx.handleCommands
import hextant.inspect.Inspections
import hextant.serial.EditorAccessor
import hextant.serial.PropertyAccessor
import hextant.serial.json
import javafx.css.PseudoClass
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.MouseEvent
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer
import reaktive.Observer
import reaktive.addListener
import reaktive.observe
import reaktive.value.ReactiveValue
import reaktive.value.now
import reaktive.value.reactiveVariable
import kotlin.reflect.KProperty

/**
 * An [EditorView] represented as a [javafx.scene.control.Control]
 * @param R the type of the root-[Node] of this control
 * @property context the [Context] of this [EditorControl]
 */
abstract class EditorControl<R : Node>(
    final override val target: Editor<*>,
    val context: Context,
    final override val arguments: Bundle
) : Control(), EditorView {
    constructor(editor: Editor<*>, arguments: Bundle) : this(editor, editor.context, arguments)

    private var _root: R? = null

    private val selection = context[SelectionDistributor]

    private val inspections = context[Inspections]

    private val errorCount = inspections.errorCount(target)
    private val warningCount = inspections.warningCount(target)

    private val errorsObserver = errorCount.observe(this) { _, _, errors ->
        problemCountUpdate(errors, warningCount.now)
    }
    private val warningsObserver = warningCount.observe(this) { _, _, warnings ->
        problemCountUpdate(errorCount.now, warnings)
    }

    var editorParent: EditorControl<*>? = null
        private set

    private val editorChildren = mutableListOf<EditorControl<*>>()

    private val changedArguments = mutableMapOf<Property<*, *>, Any>()
    private val propertyChangeHandler = context[Properties.propertyChangeHandler]
    private val propertyObserver: Observer

    /**
     * Return a list of child [EditorControl]'s
     */
    open fun editorChildren(): List<EditorControl<*>> = editorChildren

    private fun indexInParent(siblings: List<EditorControl<*>>) =
        if (this in siblings) siblings.indexOf(this)
        else if (parent in siblings) siblings.indexOf(parent)
        else {
            System.err.println("Can't find $this in parent's children")
            null
        }

    internal fun next(): EditorControl<*>? {
        val siblings = editorParent?.editorChildren() ?: return null
        val idx = indexInParent(siblings) ?: return null
        return if (idx < siblings.lastIndex) siblings[idx + 1] else editorParent?.next()
    }

    internal fun previous(): EditorControl<*>? {
        val siblings = editorParent?.editorChildren() ?: return null
        val idx = indexInParent(siblings) ?: return null
        return if (idx > 0) siblings[idx - 1] else editorParent?.previous()
    }

    private val _isSelected = reactiveVariable(false)

    /**
     * A [ReactiveValue] holding `true` only if this [EditorControl] is selected at the moment
     */
    val isSelected: ReactiveValue<Boolean> get() = _isSelected

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
            root.isMouseTransparent = false
            root.addEventHandler(MouseEvent.MOUSE_CLICKED) { ev ->
                if (ev.isShiftDown) toggleSelection()
                else select()
            }
            setRoot(newRoot)
        }

    internal val argumentHandlers = mutableMapOf<Property<*, *>, MutableList<(Any) -> Unit>>()

    init {
        styleClass.add("editor-control")
        for ((p, v) in arguments.entries) {
            @Suppress("UNCHECKED_CAST")
            propertyChangeHandler.valueChanged(this, p as Property<Any, *>, v)
        }
        propertyObserver = arguments.changed.observe(this) { _, change ->
            val new = change.newValue ?: change.property.default!!
            if (!relayoutPending) {
                if (change.newValue != null) changedArguments[change.property] = change.newValue!!
                else changedArguments.remove(change.property)
            }
            @Suppress("UNCHECKED_CAST")
            val property = change.property as Property<Any, *>
            propertyChangeHandler.valueChanged(this, property, new)
            argumentHandlers[property]?.forEach { handler -> handler.invoke(new) }
            argumentChanged(property, new)
        }
        sceneProperty().addListener(this) { sc ->
            if (sc != null) problemCountUpdate(errorCount.now, warningCount.now)
        }
        isFocusTraversable = false
        initShortcuts()
    }

    internal fun initializeControl() {
        if (_root == null) root = createDefaultRoot()
        for (child in editorChildren) {
            child.initializeControl()
        }
    }

    open fun supportedParameters(): Collection<Property<*, *>> = argumentHandlers.keys

    /**
     * Is called when one of the display [arguments] changed.
     */
    internal open fun <T : Any> argumentChanged(property: Property<T, *>, value: T) {}

    fun <T : Any> addArgumentHandler(property: Property<T, *>, handler: (T) -> Unit) {
        argumentHandlers.getOrPut(property, ::mutableListOf).add(handler as (Any) -> Unit)
    }

    fun supportArguments(vararg properties: Property<*, *>) {
        for (property in properties) {
            if (property !in supportedParameters()) {
                argumentHandlers[property] = mutableListOf()
            }
        }
    }

    internal open fun setEditorParent(parent: EditorControl<*>?) {
        editorParent = parent
    }

    /**
     * Defines the list of children of this [EditorControl].
     * For all children their parent is set to this [EditorControl].
     * The left and right [EditorControl]s of the children are set according to their order in the list.
     */
    protected open fun setChildren(children: List<EditorControl<*>>) {
        editorChildren.clear()
        if (children.isEmpty()) return
        editorChildren.addAll(children)
        children.forEach { ch -> ch.setEditorParent(this) }
    }

    /**
     * Make the given [EditorControl] a child of this editor control.
     */
    protected open fun addChild(child: EditorControl<*>, idx: Int) {
        editorChildren.add(idx, child)
        child.setEditorParent(this)
    }

    private fun clearChildren() {
        editorChildren.clear()
    }

    /**
     * Remove the editor child at the given [index]
     */
    protected open fun removeChild(index: Int) {
        editorChildren.removeAt(index)
    }

    protected fun swapChildren(i: Int, j: Int) {
        val tmp = editorChildren[i]
        editorChildren[i] = editorChildren[j]
        editorChildren[j] = tmp
    }

    /**
     * Delegates to [setChildren]
     */
    protected fun setChildren(vararg children: EditorControl<*>) {
        setChildren(children.asList())
    }

    fun getChild(accessor: EditorAccessor): EditorControl<*>? {
        val editor = target.getSubEditor(accessor)
        return editorChildren().find { it.target == editor }
    }

    fun getChild(property: KProperty<*>) = getChild(PropertyAccessor(property.name))

    /**
     * Creates the default root for this control
     */
    protected abstract fun createDefaultRoot(): R

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
     * The default implementation just calls [select].
     */
    override fun receiveFocus() {
        select()
    }

    /**
     * Delegates to the [EditorControl.root]
     */
    override fun requestFocus() {
        focus()
    }

    /**
     * Select this editor control and request focus.
     */
    override fun select() {
        selection.select(this)
    }

    /**
     * Toggle the selection of this [EditorControl] and request focus if it is selected afterwards
     */
    override fun toggleSelection() {
        selection.toggleSelection(this)
    }

    override fun displaySelected(status: Boolean) {
        _isSelected.set(status)
        root.pseudoClassStateChanged(PseudoClasses.SELECTED, status)
    }

    override fun changePseudoClassState(pseudoClass: PseudoClass, active: Boolean) {
        pseudoClassStateChanged(pseudoClass, active)
    }

    private fun initShortcuts() {
        registerShortcuts(this) {
            if (context[SelectionDistributor].selectedViews.now.contains(receiver)) {
                handleCommands(receiver, context, context[Properties.localCommandLine])
            }
            if (context[SelectionDistributor].selectedTargets.now.contains(receiver.target)) {
                handleCommands(receiver.target, context, context[Properties.localCommandLine])
            }
        }
    }

    protected fun copy(): Boolean = target.copyToClipboard()

    protected fun paste(): Boolean = target.pasteFromClipboard()

    fun showInspections(): Boolean {
        val inspectionPopup = InspectionPopup(context, target)
        inspectionPopup.show(root)
        return inspectionPopup.isShowing
    }

    fun shrinkSelection() {
        val childToSelect = lastExtendingChild ?: editorChildren().firstOrNull() ?: return
        childToSelect.select()
        if (isSelected.now) toggleSelection()
    }

    fun extendSelection() {
        var parent = editorParent ?: return
        while (parent is WrappingEditorControl || (parent is ListEditorControl && parent.editorChildren().size == 1)) {
            parent = parent.editorParent ?: return
        }
        parent.select()
        if (isSelected.now) toggleSelection()
        parent.lastExtendingChild = this
    }

    private fun problemCountUpdate(errors: Int, warnings: Int) {
        val error = errors > 0
        val warn = errors <= 0 && warnings > 0
        if (error && "error" !in styleClass) {
            styleClass.add("error")
        } else if (!error) styleClass.remove("error")
        if (warn && "warning" !in styleClass) {
            styleClass.add("warning")
        } else if (!warn) styleClass.remove("warning")
    }

    fun exportJsonArgumentTree(): JsonObject = buildJsonObject {
        for ((key, value) in changedArguments) {
            val type = key.propertyType ?: error("No type for $key")
            val serializer = serializer(type) as KSerializer<Any>
            put(key.name, json.encodeToJsonElement(serializer, value))
        }
        for (child in editorChildren()) {
            val accessor = child.target.accessor
            val args = child.exportJsonArgumentTree()
            if (args.isEmpty()) continue
            val field = accessor.toString()
            put(field, args)
        }
    }


    fun importJsonArgumentTree(tree: JsonObject) {
        for (property in supportedParameters()) {
            if (property.name in tree) {
                val type = property.propertyType ?: error("No type for $property")
                val serializer = serializer(type) as KSerializer<Any>
                val value = json.decodeFromJsonElement(serializer, tree[property.name]!!)
                arguments[Public, property as Property<Any, Public>] = value
            }
        }
        for (child in editorChildren()) {
            val accessor = child.target.accessor
            val subTree = tree[accessor.toString()] ?: continue
            if (subTree !is JsonObject) {
                System.err.println("Invalid sub tree for child $accessor of $target: $subTree")
                continue
            }
            child.importJsonArgumentTree(subTree)
        }
    }

    private var relayoutPending = false

    fun relayout(function: () -> Unit) {
        relayoutPending = true
        function()
        relayoutPending = false
    }
}
