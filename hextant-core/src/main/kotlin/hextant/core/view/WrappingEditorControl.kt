package hextant.core.view

import bundles.Bundle
import hextant.core.Editor
import hextant.serial.Snapshot
import hextant.serial.snapshot
import javafx.scene.Node
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder

abstract class WrappingEditorControl<R : Node>(
    editor: Editor<*>, arguments: Bundle
) : EditorControl<R>(editor, arguments) {
    protected var wrapped: EditorControl<*>? = null
        set(value) {
            field = value
            value?.setNext(next)
            value?.setPrevious(previous)
            value?.setEditorParent(editorParent)
        }

    override fun setEditorParent(parent: EditorControl<*>?) {
        super.setEditorParent(parent)
        wrapped?.setEditorParent(parent)
    }

    override fun setNext(nxt: EditorControl<*>?) {
        super.setNext(nxt)
        wrapped?.setNext(nxt)
    }

    override fun setPrevious(prev: EditorControl<*>?) {
        super.setPrevious(prev)
        wrapped?.setNext(prev)
    }

    override fun editorChildren(): List<EditorControl<*>> = wrapped?.let { listOf(it) } ?: emptyList()

    override fun setChildren(children: List<EditorControl<*>>) {
        throw UnsupportedOperationException("Children of $this are fixed.")
    }

    override fun addChild(child: EditorControl<*>, idx: Int) {
        throw UnsupportedOperationException("Children of $this are fixed.")
    }

    override fun removeChild(index: Int) {
        throw UnsupportedOperationException("Children of $this are fixed.")
    }

    private class Snap : AbstractSnap<ExpanderControl>() {
        private var content: Snapshot<EditorControl<*>>? = null

        override fun doRecord(original: ExpanderControl) {
            content = original.wrapped?.snapshot()
        }

        override fun reconstructObject(original: ExpanderControl) {
            if (original.wrapped != null) content?.reconstructObject(original.wrapped!!)
        }

        override fun encode(builder: JsonObjectBuilder) {
            builder.put("content", this.content?.encodeToJson() ?: JsonNull)
        }

        override fun decode(element: JsonObject) {
            content = element["content"]?.let { decodeFromJson<EditorControl<*>>(it) }
        }
    }
}