/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.*
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.ListEditor
import hextant.fx.*
import hextant.fx.ModifierValue.DOWN
import hextant.fx.ModifierValue.MAYBE
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.FontAwesome.Glyph.PLUS

/**
 * Objects of this class are used to display [ListEditor]s.
 */
open class ListEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    private val editor: ListEditor<*, *>, args: Bundle
) : ListEditorView, EditorControl<Node>(editor, args) {
    constructor(editor: ListEditor<*, *>, args: Bundle, orientation: Orientation) : this(editor, args.apply {
        set(ORIENTATION, orientation)
    })

    private val emptyDisplay = arguments[EMPTY_DISPLAY].invoke()

    /**
     * The orientation in which the sub-editors are displayed.
     */
    var orientation by arguments.property(ORIENTATION)

    /**
     * The cell factory that is used to create [Cell]s for individual editors.
     */
    var cellFactory: () -> Cell<*> by arguments.property(CELL_FACTORY)

    private var items = orientation.createLayout()

    private fun orientationChanged(new: Orientation) {
        items = new.createLayout()
        addChildren()
    }

    private fun addChildren() {
        for (c in cells) {
            items.children.add(c)
        }
    }

    private fun cellFactoryChanged() {
        cells.clear()
        cells.addAll(cells(editor.editors.now))
        items.children.clear()
        addChildren()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> argumentChanged(property: Property<T, *>, value: T) {
        when (property) {
            ORIENTATION -> orientationChanged(value as Orientation)
            CELL_FACTORY -> cellFactoryChanged()
        }
    }

    private val cells = mutableListOf<Cell<*>>()

    private fun cells(items: List<Editor<*>>) =
        items.mapIndexedTo(mutableListOf()) { idx, e -> getCell(idx, context.createControl(e)) }

    init {
        editor.addView(this)
        initEmptyDisplay()
    }

    private fun initEmptyDisplay() {
        emptyDisplay.setOnMouseClicked {
            editor.addAt(0)
        }
        registerShortcuts {
            on(PASTE_MANY) { editor.pasteManyFromClipboard(0) }
        }
        emptyDisplay.registerShortcuts {
            on(ADD_ITEM_AFTER) { editor.addAt(0) }
        }
    }

    override fun createDefaultRoot(): Pane = items

    /**
     * This method may be overwritten to pass arguments to children of this list editor view
     */
    protected open fun BundleBuilder.provideChildArguments() {}

    override fun added(editor: Editor<*>, idx: Int) {
        val view = context.createControl(editor) { provideChildArguments() }
        val c = getCell(idx, view)
        cells.drop(idx).forEach { cell -> cell.index += 1 }
        cells.add(idx, c)
        items.children.add(idx, c)
        addChild(view, idx)
        c.requestFocus()
    }

    private fun getCell(idx: Int, control: EditorControl<*>): Cell<*> {
        control.root //Initialize root
        return cellFactory().apply {
            item = control
            index = idx
            initEventHandlers()
        }
    }

    private fun Cell<*>.initEventHandlers() {
        registerShortcuts {
            on(ADD_ITEM_AFTER) { editor.addAt(index + 1) }
            on(ADD_ITEM_BEFORE) { editor.addAt(index) }
            on(REMOVE_ITEM) { editor.removeAt(index) }
            on(PASTE_MANY) {
                editor.pasteManyFromClipboard(index)
            }
            if (cells.size > index + 1) on(orientation.nextCombination) { cells[index + 1].requestLayout() }
            if (cells.size > index + 1) on(orientation.previousCombination) { cells[index - 1].requestLayout() }
        }
    }

    override fun removed(idx: Int) {
        items.children.removeAt(idx)
        cells.removeAt(idx)
        cells.drop(idx).forEach { c -> c.index = c.index - 1 }
        if (idx == 0 && cells.size > 0) cells[0].requestFocus()
        else if (idx != 0) cells[idx - 1].requestFocus()
        removeChild(idx)
    }

    override fun empty() {
        items.children.clear()
        cells.clear()
        root = emptyDisplay
    }

    override fun notEmpty() {
        root = items
    }

    override fun receiveFocus() {
        val firstChild = cells.firstOrNull() ?: emptyDisplay
        firstChild.requestFocus()
    }

    /**
     * Decides whether items are displayed horizontally or vertically in a [ListEditorControl]
     */
    sealed class Orientation {
        internal abstract fun createLayout(): Pane

        internal abstract val nextCombination: Shortcut

        internal abstract val previousCombination: Shortcut

        /**
         * Indicates a horizontal display of items.
         */
        object Horizontal : Orientation() {
            override fun createLayout(): Pane = HBox()

            override val nextCombination = "Right".shortcut

            override val previousCombination = "Left".shortcut
        }

        /**
         * Indicates a vertical display of items.
         */
        object Vertical : Orientation() {
            override fun createLayout(): Pane = VBox()

            override val nextCombination = "Down".shortcut

            override val previousCombination = "Up".shortcut
        }
    }

    /**
     * Superclass for cells that are used to display editors in a [ListEditorControl]
     */
    abstract class Cell<R : Node> : Control() {
        /**
         * The index of the editor in the [ListEditor]
         */
        var index: Int = -1
            internal set(value) {
                field = value
                updateIndex(value)
            }

        /**
         * The [EditorControl] that displays the editor
         */
        var item: EditorControl<*>? = null
            internal set(value) {
                value!!
                field = value
                updateItem(value)
            }

        private var _root: R? = null

        /**
         * The node that displays the item
         */
        var root: R
            get() = _root ?: throw IllegalStateException("Root not initialized")
            protected set(value) {
                _root = value
                setRoot(value)
            }

        @Suppress("KDocMissingDocumentation")
        override fun requestFocus() {
            item?.receiveFocus()
        }

        /**
         * This method is called when the [index] changes.
         */
        protected open fun updateIndex(idx: Int) {}

        /**
         * This method is called when the [item] is updated.
         */
        protected open fun updateItem(item: EditorControl<*>) {}

        companion object {
            fun <N : Node> create(displayItem: (control: EditorControl<*>) -> N) = object : Cell<N>() {
                override fun updateItem(item: EditorControl<*>) {
                    root = displayItem(item)
                }
            }
        }
    }


    /**
     * The default type of cell. It just displays the editor control of the contained editor.
     */
    class DefaultCell(private val displayItem: (control: EditorControl<*>) -> Node = { it }) : Cell<Node>() {
        override fun updateItem(item: EditorControl<*>) {
            root = displayItem(item)
        }
    }

    /**
     * The numbered cell prefixes the view of the editor with its 1-based index in the list.
     */
    open class NumberedCell(
        private val startIndex: Int = 1,
        private val displayItem: (control: EditorControl<*>) -> Node = { it }
    ) : Cell<HBox>() {
        private val label = Label().withStyleClass("editor-list-number")

        init {
            root = HBox(label, Region()).withStyleClass("numbered-cell")
        }

        override fun updateIndex(idx: Int) {
            label.text = "${idx + startIndex}."
        }

        override fun updateItem(item: EditorControl<*>) {
            root.children[1] = displayItem(item)
        }
    }

    /**
     * The prefix cell displays the view of the editor right of a given prefix-[Node].
     */
    class PrefixCell(
        prefix: Node,
        private val displayItem: (control: EditorControl<*>) -> Node = { it }
    ) : Cell<HBox>() {
        constructor(text: String) : this(Label(text))

        init {
            root = HBox(prefix, Region()).withStyleClass("prefix-cell")
        }

        override fun updateItem(item: EditorControl<*>) {
            root.children[1] = displayItem(item)
        }
    }

    /**
     * The separator cell separates editor views with a given [separator] node.
     */
    class SeparatorCell(
        private val separator: Node,
        private val displayItem: (control: EditorControl<*>) -> Node = { it }
    ) : Cell<HBox>() {
        constructor(text: String) : this(Label(text))

        init {
            root = HBox(Region(), Region()).withStyleClass("separator-cell")
        }

        override fun updateIndex(idx: Int) {
            root.children[0] = if (idx != 0) separator else Region()
        }

        override fun updateItem(item: EditorControl<*>) {
            root.children[1] = displayItem(item)
        }
    }

    companion object {
        private val ADD_ITEM_AFTER = shortcut(KeyCode.INSERT) { control(MAYBE) }

        private val ADD_ITEM_BEFORE = shortcut(KeyCode.INSERT) { shift(DOWN); control(MAYBE) }

        private val REMOVE_ITEM = shortcut(KeyCode.DELETE) { control(MAYBE) }

        private const val PASTE_MANY = "Ctrl + Shift + V"

        /**
         * Return a [ListEditorControl] where the given [emptyText] is displayed when the [ListEditor] is empty.
         */
        fun withAltText(
            editor: ListEditor<*, *>,
            emptyText: String = "Add item",
            args: Bundle = createBundle()
        ) = ListEditorControl(editor, args.also {
            it[EMPTY_DISPLAY] = { Button(emptyText) }
        })

        /**
         * Return a [ListEditorControl] where the given [glyph] is displayed when the [ListEditor] is empty.
         */
        fun withAltGlyph(
            editor: ListEditor<*, *>,
            glyph: FontAwesome.Glyph,
            args: Bundle = createBundle(),
            orientation: Orientation = Orientation.Vertical
        ) = ListEditorControl(editor, args.also {
            it[ORIENTATION] = orientation
            it[EMPTY_DISPLAY] = { Glyphs.create(glyph) }
        })

        /**
         * The [ListEditorControl.orientation] of items
         */
        val ORIENTATION = publicProperty<Orientation>("list view orientation")

        /**
         * The [ListEditorControl.cellFactory] used to display items
         */
        val CELL_FACTORY = publicProperty<() -> Cell<*>>("list view cell factory") { DefaultCell() }

        /**
         * The [Node] that is displayed when no items are in the [ListEditor]
         */
        val EMPTY_DISPLAY = publicProperty<() -> Node>("empty display") {
            Glyphs.create(PLUS).withStyleClass("standard-empty-display")
        }
    }
}