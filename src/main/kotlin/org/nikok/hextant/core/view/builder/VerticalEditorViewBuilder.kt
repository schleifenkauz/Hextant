package org.nikok.hextant.core.view.builder

interface VerticalEditorViewBuilder: EditorViewBuilder {
    fun indented(block: VerticalEditorViewBuilder.() -> Unit)

    fun line(block: HorizontalEditorViewBuilder.() -> Unit)
}
