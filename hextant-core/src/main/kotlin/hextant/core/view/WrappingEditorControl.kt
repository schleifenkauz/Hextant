package hextant.core.view

import bundles.Bundle
import hextant.core.Editor
import javafx.scene.Node

abstract class WrappingEditorControl<R : Node>(
    editor: Editor<*>, arguments: Bundle
) : EditorControl<R>(editor, arguments) {
    protected var wrapped: EditorControl<*>? = null
        set(value) {
            field = value
            value?.setEditorParent(editorParent)
        }

    override fun setEditorParent(parent: EditorControl<*>?) {
        super.setEditorParent(parent)
        wrapped?.setEditorParent(parent)
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
}