package hextant.core.editor

import hextant.core.Editor
import hextant.core.view.OptionalEditorView
import hextant.serial.EditorAccessor
import hextant.serial.OptionalEditorContent
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import reaktive.value.*
import reaktive.value.binding.flatMap
import kotlin.reflect.KClass

abstract class OptionalEditor<R, E : Editor<R>>() : AbstractEditor<R, OptionalEditorView>() {
    protected abstract val default: R

    private lateinit var _editor: ReactiveVariable<E?>

    val content: ReactiveValue<E?> get() = _editor

    val isExpanded get() = content.now != null

    protected abstract fun createEditor(): E

    override fun setupDefaultState() {
        _editor = reactiveVariable(null)
    }

    final override lateinit var result: ReactiveValue<R>
        private set

    override fun doInitialize() {
        result = _editor.flatMap { it?.result ?: reactiveValue(default) }
    }

    fun reset() {
        if (!isExpanded) {
            System.err.println("Warning: $this is already reset")
            return
        }
        val contentRef = content.now!!
        doReset()
        context[UndoManager].record(Reset(this, contentRef))
    }

    fun expand() {
        if (isExpanded) {
            System.err.println("Warning: $this is already expanded")
            return
        }
        doExpand()
        context[UndoManager].record(Expand(this))
    }

    private fun doReset() {
        _editor.set(null)
        notifyViews { removed() }
    }

    private fun doExpand() {
        val editor = createEditor()
        setContent(editor)
        notifyViews { focus() }
    }

    private fun setContent(content: E) {
        content.initialize(context, parent = this, OptionalEditorContent)
        _editor.set(content)
        notifyViews { display(content) }
    }

    fun setInitialContent(content: E?) {
        _editor = reactiveVariable(content)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> = when (accessor) {
        OptionalEditorContent -> content.now ?: super.getSubEditor(accessor)
        else -> super.getSubEditor(accessor)
    }

    override fun serialize(): JsonElement =
        content.now?.serialize(typeTag = fixedContentClass() == null) ?: JsonNull

    override fun deserialize(element: JsonElement) {
        if (element == JsonNull) {
            setInitialContent(null)
            return
        }
        val klass = fixedContentClass()
        val editor = Editor.deserialize(element, klass)
        setInitialContent(editor as E)
    }

    protected open fun fixedContentClass(): KClass<E>? = null

    private class Expand(private val ref: OptionalEditor<*, *>) : AbstractEdit() {
        override fun doRedo() {
            ref.doExpand()
        }

        override fun doUndo() {
            ref.doReset()
        }

        override val actionDescription: String
            get() = "expand"
    }

    private class Reset<E : Editor<*>>(
        private val ref: OptionalEditor<*, E>,
        private val content: E
    ) : AbstractEdit() {
        override fun doRedo() {
            ref.doReset()
        }

        override fun doUndo() {
            ref.setContent(content)
        }

        override val actionDescription: String
            get() = "reset"
    }
}