/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.Property
import bundles.createBundle
import hextant.context.createControl
import hextant.core.Editor
import hextant.fx.Glyphs
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import reaktive.Observer
import reaktive.Reactive

/**
 * A [CompoundEditorControl] is an [EditorControl] composed of multiple
 * [EditorControl]'s, keywords, operators, lines and spaces
 */
abstract class CompoundEditorControl(
    editor: Editor<*>,
    args: Bundle,
) : EditorControl<Node>(editor, args) {
    private val cachedViews = mutableMapOf<Editor<*>, EditorControl<*>>()

    private var firstChildToFocus: EditorControl<*>? = null

    private val observers = mutableListOf<Observer>()

    protected abstract fun build(): Layout

    override fun createDefaultRoot(): Node {
        val layout = build()
        if (layout.firstEditorChild != null) firstChildToFocus = layout.firstEditorChild
        setChildren(layout.editorChildren)
        return if (layout.root.children.size == 1) layout.root.children[0] else layout.root
    }

    override fun <T : Any> argumentChanged(property: Property<T, *>, value: T) {
        root = createDefaultRoot()
    }

    override fun receiveFocus() {
        firstChildToFocus?.receiveFocus()
    }

    protected fun triggerLayoutOnChange(reactive: Reactive) {
        val obs = reactive.observe {
            root = createDefaultRoot()
        }
        observers.add(obs)
    }

    fun vertical(block: Vertical.() -> Unit) = Vertical(cachedViews, mutableListOf()).apply(block)

    fun horizontal(block: Horizontal.() -> Unit) = Horizontal(cachedViews, mutableListOf()).apply(block)

    fun getSubControl(editor: Editor<*>) = cachedViews[editor]

    /**
     * Base interface for [Vertical] and [Horizontal] boxes
     */
    abstract class Layout internal constructor(
        @PublishedApi internal val cachedViews: MutableMap<Editor<*>, EditorControl<*>>,
        @PublishedApi internal val editorChildren: MutableList<EditorControl<*>>
    ) {
        @PublishedApi
        internal var firstEditorChild: EditorControl<*>? = null

        abstract val root: Pane

        /**
         * Creates a view for the given [editor] with the specified [args] and add it to this compound control
         * @return the created view for further configuration
         */
        fun view(editor: Editor<*>, args: Bundle = createBundle(), cached: Boolean = true): EditorControl<*> {
            val control =
                if (cached && editor in cachedViews) cachedViews.getValue(editor)
                else editor.context.createControl(editor, args)
            if (cached) cachedViews[editor] = control
            if (firstEditorChild == null) firstEditorChild = control
            root.children.add(control)
            editorChildren.add(control)
            return control
        }

        /**
         * Add a [Label] containing a single space to this compound control
         * @return the created [Label] for further configuration
         */
        fun space(): Label {
            val l = Label(" ")
            root.children.add(l)
            return l
        }

        /**
         * Add a [Node] displaying the given keyword to this compound control.
         * The resulting node has the `keyword` style-class.
         * @return the created [Node] for further configuration
         */
        fun keyword(name: String): Node {
            val l = hextant.fx.keyword(name)
            root.children.add(l)
            return l
        }

        /**
         * Add a [Node] displaying the given operator to this compound control.
         * The resulting node has the `operator` style-class.
         * @return the created [Node] for further configuration
         */
        fun operator(str: String): Node {
            val l = hextant.fx.operator(str)
            root.children.add(l)
            return l
        }

        /**
         * Add the given [Node] to this compound control and return it.
         */
        fun <N : Node> add(node: N): N {
            if (node is EditorControl<*>) {
                if (firstEditorChild == null) firstEditorChild = node
                editorChildren.add(node)
            }
            root.children.add(node)
            return node
        }

        /**
         * Add the given [Glyph] to this compound control and return it.
         */
        fun icon(glyph: FontAwesome.Glyph): Glyph {
            val g = Glyphs.create(glyph)
            root.children.add(g)
            return g
        }

        fun styleClass(vararg names: String) {
            root.styleClass.addAll(*names)
        }
    }

    /**
     * A vertical box
     */
    class Vertical @PublishedApi internal constructor(
        cachedViews: MutableMap<Editor<*>, EditorControl<*>>,
        editorChildren: MutableList<EditorControl<*>>
    ) : Layout(cachedViews, editorChildren) {
        override val root: VBox = VBox()

        var spacing
            get() = root.spacing
            set(value) {
                root.spacing = value
            }

        /**
         * Add a [Horizontal] box to this control and configure it with the given [build] block.
         */
        fun line(spacing: Double = 0.0, alignment: Pos = Pos.CENTER_LEFT, build: Horizontal.() -> Unit): Horizontal =
            horizontal(spacing, alignment, build)

        inline fun horizontal(
            spacing: Double = 0.0,
            alignment: Pos = Pos.CENTER_LEFT,
            build: Horizontal.() -> Unit
        ): Horizontal {
            val horizontal = Horizontal(cachedViews, editorChildren).apply(build)
            if (horizontal.firstEditorChild != null && this.firstEditorChild == null)
                this.firstEditorChild = horizontal.firstEditorChild
            horizontal.root.spacing = spacing
            horizontal.root.alignment = alignment
            root.children.add(horizontal.root)
            return horizontal
        }

        /**
         * Create a [Vertical] box configured with [build] and add it together with some leading space to this box.
         */
        inline fun indented(build: Vertical.() -> Unit): HBox {
            val indent = Label("  ")
            val v = Vertical(cachedViews, editorChildren).apply(build)
            if (v.firstEditorChild != null && this.firstEditorChild == null)
                this.firstEditorChild = v.firstEditorChild
            val indented = HBox(indent, v.root)
            root.children.add(indented)
            return indented
        }
    }

    /**
     * A horizontal box
     */
    class Horizontal @PublishedApi internal constructor(
        cachedViews: MutableMap<Editor<*>, EditorControl<*>>,
        editorChildren: MutableList<EditorControl<*>>
    ) : Layout(cachedViews, editorChildren) {
        override val root: HBox = HBox()

        var spacing
            get() = root.spacing
            set(value) {
                root.spacing = value
            }

        inline fun vertical(
            spacing: Double = 0.0,
            build: Vertical.() -> Unit
        ): Vertical {
            val vertical = Vertical(cachedViews, editorChildren).apply(build)
            if (vertical.firstEditorChild != null && this.firstEditorChild == null)
                this.firstEditorChild = vertical.firstEditorChild
            vertical.root.spacing = spacing
            root.children.add(vertical.root)
            return vertical
        }
    }

    companion object {
        /**
         * Create a [CompoundEditorControl] for the [editor] which lays its components out using [buildLayout].
         * Any changes emitted from the [layoutTriggers] cause the [buildLayout] function to be reevaluated.
         */
        operator fun invoke(
            editor: Editor<*>,
            args: Bundle = createBundle(),
            vararg layoutTriggers: Reactive,
            buildLayout: CompoundEditorControl.() -> Layout,
        ): CompoundEditorControl = object : CompoundEditorControl(editor, args) {
            init {
                for (trigger in layoutTriggers) {
                    triggerLayoutOnChange(trigger)
                }
            }

            override fun build(): Layout = buildLayout()
        }
    }
}