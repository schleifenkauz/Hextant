/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.base.CompoundEditorControl.Vertical
import hextant.bundle.Bundle
import hextant.bundle.Property
import hextant.fx.keyword
import hextant.fx.operator
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.*

abstract class CompoundEditorControl(
    editable: Editable<*>,
    private val context: Context,
    private val args: Bundle,
    private val build: Vertical.(args: Bundle) -> Unit
) : EditorControl<Vertical>(args) {
    private var firstChildToFocus: EditorControl<*>? = null

    init {
        val editor: AbstractEditor<*, EditorView> = context.getEditor(editable)
        initialize(editable, editor, context)
        editor.addView(this)
    }

    override fun createDefaultRoot(): Vertical = Vertical().apply {
        build(args)
    }.also {
        if (it.firstEditorChild != null) firstChildToFocus = it.firstEditorChild
    }

    override fun receiveFocus() {
        firstChildToFocus?.receiveFocus() ?: this.requestFocus()
    }

    override fun argumentChanged(property: Property<*, *, *>, value: Any?) {
        root = createDefaultRoot()
    }

    interface Compound {
        fun view(editable: Editable<*>, args: Bundle = Bundle.newInstance()): EditorControl<*>

        fun space(): Label

        fun keyword(name: String): Node

        fun operator(str: String): Node
    }

    inner class Vertical : VBox(), Compound {
        var firstEditorChild: EditorControl<*>? = null
            private set

        override fun view(editable: Editable<*>, args: Bundle): EditorControl<*> =
            view(editable, this, context, args).also {
                if (firstEditorChild == null) firstEditorChild = it
            }

        override fun space() = space(this)

        override fun keyword(name: String): Node = keyword(name, this)

        override fun operator(str: String): Node = operator(str, this)

        fun line(build: Horizontal.() -> Unit): Horizontal {
            val horizontal = Horizontal().apply(build)
            if (horizontal.firstEditorChild != null && this.firstEditorChild == null)
                this.firstEditorChild = horizontal.firstEditorChild
            children.add(horizontal)
            return horizontal
        }

        fun indented(build: Vertical.() -> Unit): HBox {
            val indent = Label("  ")
            val v = Vertical().apply(build)
            if (v.firstEditorChild != null && this.firstEditorChild == null)
                this.firstEditorChild = v.firstEditorChild
            val indented = HBox(indent, v)
            children.add(indented)
            return indented
        }
    }

    inner class Horizontal : HBox(), Compound {
        var firstEditorChild: EditorControl<*>? = null
            private set

        override fun view(editable: Editable<*>, args: Bundle): EditorControl<*> =
            view(editable, this, context, args).also {
                if (firstEditorChild == null) firstEditorChild = it
            }

        override fun space() = space(this)

        override fun keyword(name: String): Node = keyword(name, this)

        override fun operator(str: String): Node = operator(str, this)
    }

    companion object {
        private fun view(
            editable: Editable<*>,
            pane: Pane,
            context: Context,
            args: Bundle
        ): EditorControl<*> {
            val c = context.createView(editable, args)
            pane.children.add(c)
            return c
        }

        private fun keyword(name: String, pane: Pane): Node {
            val l = keyword(name)
            pane.children.add(l)
            return l
        }

        private fun operator(name: String, pane: Pane): Node {
            val l = operator(name)
            pane.children.add(l)
            return l
        }

        private fun space(pane: Pane): Label {
            val l = Label(" ")
            pane.children.add(l)
            return l
        }

        operator fun invoke(
            editable: Editable<*>,
            context: Context,
            args: Bundle = Bundle.newInstance(),
            build: Vertical.(Bundle) -> Unit
        ): CompoundEditorControl = object : CompoundEditorControl(editable, context, args, build) {

        }
    }
}