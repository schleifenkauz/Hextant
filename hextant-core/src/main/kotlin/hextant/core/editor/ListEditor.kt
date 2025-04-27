/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.codegen.ProvideFeature
import hextant.context.Clipboard
import hextant.context.ClipboardContent.MultipleEditors
import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.core.view.ListEditorView
import hextant.serial.EditorAccessor
import hextant.serial.IndexAccessor
import hextant.serial.InvalidAccessorException
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import reaktive.Observer
import reaktive.list.MutableReactiveList
import reaktive.list.ReactiveList
import reaktive.list.toReactiveList
import reaktive.value.ReactiveValue
import reaktive.value.ReactiveVariable
import reaktive.value.now
import reaktive.value.reactiveVariable
import kotlin.reflect.KClass

/**
 * An editor for multiple child editors of type [E] whose result type is [R]
 */
@ProvideFeature
abstract class ListEditor<R, E : Editor<R>> : AbstractEditor<List<R>, ListEditorView>() {
    private lateinit var _editors: MutableReactiveList<E>
    private lateinit var _result: ReactiveVariable<List<R>>
    private val resultObservers = mutableListOf<Observer>()
    private val editorClass = javaClass.getMethod("createEditor").returnType.kotlin

    private var mayBeEmpty = true

    private fun mayRemove() = mayBeEmpty || editors.now.size > 1

    /**
     * All child editors of this [ListEditor]
     */
    val editors: ReactiveList<E> get() = _editors

    final override val result: ReactiveValue<List<R>>
        get() = _result

    override fun getChildren(): Collection<Editor<*>> = editors.now

    override fun doInitialize() {
        for ((idx, editor) in getChildren().withIndex()) {
            editor.initialize(childContext(), parent = this, IndexAccessor(idx))
        }
        _result = reactiveVariable(computeResultList())
        for (editor in editors.now) {
            resultObservers.add(editor.result.observe { _ ->
                _result.now = computeResultList()
            })
        }
    }

    private fun computeResultList() = editors.now.map { editor -> editor.result.now }

    fun setInitialEditors(editors: List<E>) {
        _editors = editors.toReactiveList()
    }

    fun setInitialEditors(vararg editors: E) {
        setInitialEditors(editors.toList())
    }

    override fun setupDefaultState() {
        setInitialEditors(emptyList<E>())
    }

    /**
     * Create a new Editor for results of type [E], or null if no new editor should be created
     */
    protected abstract fun createEditor(): E?

    private fun tryCreateEditor(): E? = context.executeSafely("create editor", null) { createEditor() }

    /**
     * Return the [Context] used for children of this [ListEditor].
     * The default implementation simply returns the [context] of this editor.
     */
    protected open fun childContext(): Context = context

    /**
     * Clears the children and then adds all the given new children.
     */
    fun setEditors(editors: List<E>): Boolean {
        if (!mayBeEmpty && editors.isEmpty()) return false
        doClear()
        for ((i, e) in editors.withIndex()) {
            doAddAt(i, e)
        }
        _result.now = computeResultList()
        return true
    }

    /**
     * Removes all editors. The method has no effect if [ensureNotEmpty] has been called before.
     */
    fun clear(undoable: Boolean = true) {
        if (!mayBeEmpty) return
        if (undoable) {
            val snapshots = editors.now.map { e -> e.snapshot() }
            val edit = ClearEdit(this, snapshots)
            context[UndoManager].record(edit)
        }
        doClear()
        _result.now = emptyList()
    }

    private fun doClear() {
        for (e in editors.now) {
            context.executeSafely("clearing editors", Unit) { editorRemoved(e, 0) }
        }
        resultObservers.forEach { obs -> obs.kill() }
        resultObservers.clear()
        _editors.now.clear()
        notifyViews { empty() }
    }

    /**
     * Insert all the given [editors] at the specified [idx] into this [ListEditor].
     */
    fun pasteMany(idx: Int, editors: List<E>, undoable: Boolean = true) {
        if (undoable) {
            val edit = PasteManyEdit(this, idx, editors.map { e -> e.snapshot() })
            context[UndoManager].record(edit)
        }
        if (editors.any { !editorClass.isInstance(it) }) return
        for ((i, e) in editors.withIndex()) {
            doAddAt(idx + i, e)
        }
        _result.now = computeResultList()
    }

    /**
     * Inserts all the editors currently copied with at the specified [idx] into this [ListEditor].
     */
    fun pasteManyFromClipboard(idx: Int) {
        val content = context[Clipboard].get()
        if (content !is MultipleEditors) return
        if (!content.editors.all { e -> editorClass.isInstance(e) }) return
        @Suppress("UNCHECKED_CAST")
        pasteMany(idx, content.editors as List<E>)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is IndexAccessor) throw InvalidAccessorException(accessor)
        if (accessor.index >= editors.now.size) throw InvalidAccessorException(accessor)
        return editors.now[accessor.index]
    }

    override fun paste(editor: Editor<*>): Boolean {
        TODO()
    }

    override fun supportsCopyPaste(): Boolean = editors.now.all { e -> e.supportsCopyPaste() }

    /**
     * Add a new editor at the given index using [createEditor] to create a new editor
     */
    fun addAt(index: Int): E? {
        val editor = tryCreateEditor() ?: return null
        if (context[UndoManager].isActive) {
            val edit = AddEdit(this, index, editor.snapshot())
            context[UndoManager].record(edit)
        }
        doAddAt(index, editor)
        _result.now = computeResultList()
        return editor
    }

    /**
     * Add the specified uninitalized [editor] at the specified [index]
     */
    fun addAt(index: Int, editor: E) {
        if (context[UndoManager].isActive) {
            val edit = AddEdit(this, index, editor.snapshot())
            context[UndoManager].record(edit)
        }
        doAddAt(index, editor)
        _result.now = computeResultList()
    }

    /**
     * Add the new [editor] at the end of the editor list
     */
    fun addLast(editor: E) {
        addAt(editors.now.size, editor)
        _result.now = computeResultList()
    }

    /**
     * Create a new editor with [createEditor] and insert it as the last editor.
     */
    fun addLast(): Boolean {
        val e = tryCreateEditor() ?: return false
        addLast(e)
        return true
    }

    /**
     * Remove the editor at the specified [index]
     */
    fun removeAt(index: Int) {
        if (!mayRemove()) return
        val old = _editors.now.removeAt(index)
        val observer = resultObservers.removeAt(index)
        observer.kill()
        updateIndicesFrom(index)
        notifyViews { removed(index) }
        if (emptyNow()) notifyViews { empty() }
        context.executeSafely("removing editor", Unit) { editorRemoved(old, index) }
        if (context[UndoManager].isActive) {
            val edit = RemoveEdit(this, index, old.snapshot())
            context[UndoManager].record(edit)
        }
        _result.now = computeResultList()
    }

    /**
     * Remove the given [editor] from this [ListEditor].
     */
    fun remove(editor: E) {
        val idx = editors.now.indexOf(editor)
        removeAt(idx)
    }

    fun swap(i: Int, j: Int) {
        if (i == j) return
        if (i !in editors.now.indices || j !in editors.now.indices) return
        val tmpObs = resultObservers[i]
        resultObservers[i] = resultObservers[j]
        resultObservers[j] = tmpObs
        val e = editors.now[i]
        val f = editors.now[j]
        _editors.now[i] = f
        _editors.now[j] = e
        e.setAccessor(IndexAccessor(j))
        f.setAccessor(IndexAccessor(i))
        context[UndoManager].record(SwapEdit(this, i, j))
        notifyViews { swapped(i, j) }
        _result.now = computeResultList()
    }

    /**
     * Ensures that this list of editors is not empty now and will never be.
     * Repeated calls have no effect.
     */
    fun ensureNotEmpty() {
        if (!mayBeEmpty) return //already called
        mayBeEmpty = false
        if (editors.now.isEmpty()) {
            val e = tryCreateEditor() ?: error("createEditor() returned null")
            doAddAt(0, e)
        }
    }

    /**
     * Is called whenever a new editor is added to the list of editors forming this [ListEditor]
     */
    protected open fun editorAdded(editor: E, index: Int) {}

    /**
     * Is called whenever an editor removed from the list of editors forming this [ListEditor]
     */
    protected open fun editorRemoved(editor: E, index: Int) {}

    /**
     * Adds the given [editor] at the specified [index].
     */
    private fun doAddAt(index: Int, editor: E) {
        val emptyBefore = emptyNow()
        editor.initialize(childContext(), parent = this, IndexAccessor(index))
        _editors.now.add(index, editor)
        resultObservers.add(index, editor.result.observe { _ -> _result.now = computeResultList() })
        updateIndicesFrom(index + 1)
        notifyViews {
            if (emptyBefore) notEmpty()
            added(editor, index)
        }
        context.executeSafely("adding editor", Unit) { editorAdded(editor, index) }
    }

    private fun updateIndicesFrom(index: Int) {
        for (i in index until editors.now.size) {
            val acc = editors.now[i].accessor
            if (acc !is IndexAccessor) {
                //TODO warn
                continue
            }
            acc.index = i
        }
    }

    override fun viewAdded(view: ListEditorView) {
        if (emptyNow()) view.empty()
        else editors.now.forEachIndexed { i, editor ->
            view.added(editor, i)
        }
    }

    private fun emptyNow(): Boolean = editors.now.isEmpty()

    override fun serialize(): JsonElement {
        val typeTag = fixedEditorClass() == null
        return JsonArray(editors.now.map { e -> e.serialize(typeTag = typeTag) })
    }

    override fun deserialize(element: JsonElement) {
        element as JsonArray
        val klass = fixedEditorClass()
        val editors = element.map { json -> Editor.deserialize(json, klass) as E }
        _editors = editors.toReactiveList()
    }

    protected open fun fixedEditorClass(): KClass<E>? = null

    private class AddEdit<E : Editor<*>>(
        private val editor: ListEditor<*, E>,
        private val index: Int,
        private val added: E
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.addAt(index, added)
        }

        override fun doUndo() {
            editor.removeAt(index)
        }

        override val actionDescription: String
            get() = "Add element"
    }

    private class RemoveEdit<E : Editor<*>>(
        private val editor: ListEditor<*, E>,
        private val index: Int,
        private val removed: E
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.removeAt(index)
        }

        override fun doUndo() {
            val e = editor
            e.addAt(index, removed)
        }

        override val actionDescription: String
            get() = "Remove element"
    }

    private class SwapEdit<E : Editor<*>>(
        private val editor: ListEditor<*, E>,
        private val index1: Int,
        private val index2: Int,
    ) : AbstractEdit() {
        override val actionDescription: String
            get() = "Swap"

        override fun doUndo() {
            editor.swap(index1, index2)
        }

        override fun doRedo() {
            editor.swap(index1, index2)
        }
    }

    private class ClearEdit<E : Editor<*>>(
        private val editor: ListEditor<*, E>,
        private val removed: List<E>
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.clear()
        }

        override fun doUndo() {
            editor.pasteMany(0, removed, undoable = false)
        }

        override val actionDescription: String
            get() = "Clear"
    }

    private class PasteManyEdit<E : Editor<*>>(
        private val editor: ListEditor<*, E>,
        private val index: Int,
        private val pasted: List<E>
    ) : AbstractEdit() {
        override fun doRedo() {
            editor.pasteMany(index, pasted, undoable = false)
        }

        override fun doUndo() {
            repeat(pasted.size) {
                editor.removeAt(index)
            }
        }

        override val actionDescription: String
            get() = "Paste many"
    }
}