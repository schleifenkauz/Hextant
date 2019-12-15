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

/**
 * A [CompoundEditorControl] is an [EditorControl] composed of multiple
 * [EditorControl]'s, keywords, operators, lines and spaces
 */
abstract class CompoundEditorControl(
    editor: Editor<*>,
    args: Bundle,
    private val build: Vertical.(args: Bundle) -> Unit
) : EditorControl<Vertical>(editor, args) {
    private var firstChildToFocus: EditorControl<*>? = null

    override fun createDefaultRoot(): Vertical {
        val v = Vertical()
        build(v, arguments)
        if (v.firstEditorChild != null) firstChildToFocus = v.firstEditorChild
        setChildren(v.editorChildren)
        return v
    }

    override fun argumentChanged(property: Property<*, *, *>, value: Any?) {
        root = createDefaultRoot()
    }

    override fun receiveFocus() {
        firstChildToFocus?.receiveFocus()
    }

    /**
     * Base interface for [Vertical] and [Horizontal] boxes
     */
    interface Compound {
        /**
         * Creates a view for the given [editor] with the specified [args] and add it to this compound control
         * @return the created view for further configuration
         */
        fun view(editor: Editor<*>, args: Bundle = Bundle.newInstance()): EditorControl<*>

        /**
         * Add a [Label] containing a single space to this compound control
         * @return the created [Label] for further configuration
         */
        fun space(): Label

        /**
         * Add a [Node] displaying the given keyword to this compound control.
         * The resulting node has the `keyword` style-class.
         * @return the created [Node] for further configuration
         */
        fun keyword(name: String): Node

        /**
         * Add a [Node] displaying the given operator to this compound control.
         * The resulting node has the `operator` style-class.
         * @return the created [Node] for further configuration
         */
        fun operator(str: String): Node

        /**
         * Add the given [Node] to this compound control and return it.
         */
        fun <N : Node> node(node: N): N
    }

    /**
     * A vertical box
     */
    inner class Vertical : VBox(), Compound {
        internal var firstEditorChild: EditorControl<*>? = null
            private set

        internal val editorChildren: MutableList<EditorControl<*>> = mutableListOf()

        override fun view(editor: Editor<*>, args: Bundle): EditorControl<*> =
            view(editor, this, context, args).also {
                if (firstEditorChild == null) firstEditorChild = it
                editorChildren.add(it)
            }

        override fun space() = space(this)

        override fun keyword(name: String): Node = keyword(name, this)

        override fun operator(str: String): Node = operator(str, this)

        /**
         * Add a [Horizontal] box to this control and configure it with the given [build] block.
         */
        fun line(build: Horizontal.() -> Unit): Horizontal {
            val horizontal = Horizontal().apply(build)
            if (horizontal.firstEditorChild != null && this.firstEditorChild == null)
                this.firstEditorChild = horizontal.firstEditorChild
            children.add(horizontal)
            editorChildren.addAll(horizontal.editorChildren)
            return horizontal
        }

        /**
         * Create a [Vertical] box configured with [build] and add it together with some leading space to this box.
         */
        fun indented(build: Vertical.() -> Unit): HBox {
            val indent = Label("  ")
            val v = Vertical().apply(build)
            if (v.firstEditorChild != null && this.firstEditorChild == null)
                this.firstEditorChild = v.firstEditorChild
            val indented = HBox(indent, v)
            children.add(indented)
            editorChildren.addAll(v.editorChildren)
            return indented
        }

        override fun <N : Node> node(node: N): N {
            if (node is EditorControl<*>) {
                if (firstEditorChild == null) firstEditorChild = node
                editorChildren.add(node)
            }
            children.add(node)
            return node
        }
    }

    /**
     * A horizontal box
     */
    inner class Horizontal : HBox(), Compound {
        internal val editorChildren: MutableList<EditorControl<*>> = mutableListOf()

        internal var firstEditorChild: EditorControl<*>? = null
            private set

        override fun view(editor: Editor<*>, args: Bundle): EditorControl<*> =
            view(editor, this, context, args).also {
                if (firstEditorChild == null) firstEditorChild = it
                editorChildren.add(it)
            }

        override fun <N : Node> node(node: N): N {
            if (node is EditorControl<*>) {
                if (firstEditorChild == null) firstEditorChild = node
                editorChildren.add(node)
            }
            children.add(node)
            return node
        }

        override fun space() = space(this)

        override fun keyword(name: String): Node = keyword(name, this)

        override fun operator(str: String): Node = operator(str, this)
    }

    companion object {
        private fun view(
            editable: Editor<*>,
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

        /**
         * Create a [CompoundEditorControl] and apply the given [build] block to it.
         */
        fun build(
            editor: Editor<*>,
            args: Bundle = Bundle.newInstance(),
            build: Vertical.(Bundle) -> Unit
        ): CompoundEditorControl = object : CompoundEditorControl(editor, args, build) {

        }
    }
}