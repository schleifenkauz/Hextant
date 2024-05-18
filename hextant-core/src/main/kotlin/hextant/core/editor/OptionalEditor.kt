package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.view.OptionalEditorView
import hextant.serial.*
import hextant.undo.AbstractEdit
import hextant.undo.UndoManager
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import reaktive.value.ReactiveValue
import reaktive.value.binding.flatMap
import reaktive.value.now
import reaktive.value.reactiveValue
import reaktive.value.reactiveVariable

abstract class OptionalEditor<R, E : Editor<R>>(context: Context, initialContent: E? = null) :
    AbstractEditor<R, OptionalEditorView>(context) {
    protected abstract val default: R

    private val _editor = reactiveVariable<E?>(null)

    val content: ReactiveValue<E?> get() = _editor

    val isExpanded get() = content.now != null

    protected abstract fun createEditor(): E

    override val result: ReactiveValue<R> = _editor.flatMap { it?.result ?: reactiveValue(default) }

    init {
        if (initialContent != null) setContent(initialContent)
    }

    fun reset() {
        if (!isExpanded) {
            System.err.println("Warning: $this is already reset")
            return
        }
        val contentRef = content.now!!.virtualize()
        doReset()
        context[UndoManager].record(Reset(virtualize(), contentRef))
    }

    fun expand() {
        if (isExpanded) {
            System.err.println("Warning: $this is already expanded")
            return
        }
        doExpand()
        context[UndoManager].record(Expand(virtualize()))
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
        parent?.let { content.initParent(it) }
        @Suppress("DEPRECATION")
        content.setAccessor(OptionalEditorContent)
        _editor.set(content)
        notifyViews { display(content) }
    }

    @Deprecated("Treat as internal")
    override fun initParent(parent: Editor<*>) {
        @Suppress("DEPRECATION")
        super.initParent(parent)
        @Suppress("DEPRECATION")
        content.now?.initParent(parent)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> = when (accessor) {
        OptionalEditorContent -> content.now ?: super.getSubEditor(accessor)
        else -> super.getSubEditor(accessor)
    }

    override fun createSnapshot(): Snapshot<*> = Snap()

    private class Expand(private val ref: VirtualEditor<OptionalEditor<*, *>>) : AbstractEdit() {
        override fun doRedo() {
            ref.get().doExpand()
        }

        override fun doUndo() {
            ref.get().doReset()
        }

        override val actionDescription: String
            get() = "expand"
    }

    private class Reset<E : Editor<*>>(
        private val ref: VirtualEditor<OptionalEditor<*, E>>,
        private val contentRef: VirtualEditor<E>
    ) : AbstractEdit() {
        override fun doRedo() {
            ref.get().doReset()
        }

        override fun doUndo() {
            ref.get().setContent(contentRef.get())
        }

        override val actionDescription: String
            get() = "reset"
    }

    private class Snap : Snapshot<OptionalEditor<*, *>>() {
        private var content: Snapshot<Editor<*>>? = null

        override fun doRecord(original: OptionalEditor<*, *>) {
            val content = original._editor.now
            if (content != null) this.content = content.snapshot(recordClass = true)
        }

        @Suppress("UNCHECKED_CAST")
        override fun reconstructObject(original: OptionalEditor<*, *>) {
            original as OptionalEditor<*, Editor<*>>
            val reconstructed = content?.reconstructEditor(original.context)
            if (reconstructed != null) original.setContent(reconstructed)
        }

        override fun encode(builder: JsonObjectBuilder) {
            if (content != null)
                builder.put("content", json.encodeToJsonElement(Serializer, this.content!!))
        }

        @Suppress("UNCHECKED_CAST")
        override fun decode(element: JsonObject) {
            val contentObj = element["content"] ?: return
            content = json.decodeFromJsonElement(Serializer, contentObj) as Snapshot<Editor<*>>
        }
    }
}