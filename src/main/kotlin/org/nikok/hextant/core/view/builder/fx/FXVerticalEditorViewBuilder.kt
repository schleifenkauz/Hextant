package org.nikok.hextant.core.view.builder.fx

import javafx.scene.layout.*
import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.view.builder.HorizontalEditorViewBuilder
import org.nikok.hextant.core.view.builder.VerticalEditorViewBuilder

class FXVerticalEditorViewBuilder(platform: HextantPlatform, editable: Editable<*>)
    : FXEditorViewBuilder(platform, editable, VBox()), VerticalEditorViewBuilder {
    override fun indented(block: VerticalEditorViewBuilder.() -> Unit) {
        val builder = FXVerticalEditorViewBuilder(platform, editable)
        builder.block()
        val node = builder.build().node
        val indent = Pane()
        indent.prefWidth = 15.0
        val indented = HBox(indent, node)
        addChild(indented)
    }


    override fun line(block: HorizontalEditorViewBuilder.() -> Unit) {
        val builder = FXHorizontalEditorViewBuilder(platform, editable)
        val node = builder.apply(block).pane
        addChild(node)
    }
}
