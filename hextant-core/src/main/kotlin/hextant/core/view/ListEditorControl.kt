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
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
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
    var orientation by property(arguments, ORIENTATION)

    /**
     * The cell factory that is used to create [Cell]s for individual editors.
     */
    var cellFactory: () -> Cell<*> by property(arguments, CELL_FACTORY)

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
    override fun argumentChanged(property: Property<*, *, *>, value: Any?) {
        when (property) {
            ORIENTATION -> orientationChanged(value as Orientation)
            CELL_FACTORY -> cellFactoryChanged()
        }
    }

    private val cells = mutableListOf<Cell<*>>()

    private fun cells(items: List<Editor<*>>) =
        items.mapIndexedTo(mutableListOf()) { idx, e -> getCell(idx, context.createControl(e)) }

    /**
     * The default type of cell. It just displays the editor control of the contained editor.
     */
    class DefaultCell : Cell<EditorControl<*>>() {
        override fun updateItem(item: EditorControl<*>) {
            root = item
        }
    }

    /**
     * The numbered cell prefixes the view of the editor with its 1-based index in the list.
     */
    class NumberedCell : Cell<Label>() {
        init {
            root = Label()
            root.contentDisplay = RIGHT
        }

        override fun updateIndex(idx: Int) {
            root.text = "${idx + 1}."
        }

        override fun updateItem(item: EditorControl<*>) {
            root.graphic = item
        }
    }

    /**
     * The prefix cell displays the view of the editor right of a given prefix-[Node].
     */
    class PrefixCell(prefix: Node) : Cell<HBox>() {
        constructor(text: String) : this(Label(text))

        init {
            root = HBox(prefix)
        }

        override fun updateItem(item: EditorControl<*>) {
            if (root.children.size == 2) {
                root.children[1] = item
            } else if (root.children.size == 1) {
                root.children.add(1, item)
            }
        }
    }

    /**
     * The separator cell separates editor views with a given [separator] node.
     */
    class SeparatorCell(private val separator: Node) : Cell<HBox>() {
        constructor(text: String) : this(Label(text))

        private var left: Node? = null
            get() = if (root.children.size == 2) root.children[0] else null
            set(l) {
                when {
                    l == null && field != null -> root.children.removeAt(0)
                    l == null                  -> {
                    }
                    field != null              -> root.children[0] = l
                    else                       -> root.children.add(0, l)
                }
                field = l
            }

        private var right: Node
            get() = root.children[root.children.size - 1]
            set(value) {
                if (root.children.size == 2) {
                    root.children[1] = value
                } else root.children.add(root.children.size, value)
            }

        init {
            root = HBox()
        }

        override fun updateIndex(idx: Int) {
            left = if (idx != 0) separator
            else null
        }

        override fun updateItem(item: EditorControl<*>) {
            right = item
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
        protected var root: R
            get() = _root ?: throw IllegalStateException("Root not initialized")
            set(value) {
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
    }

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
    protected open fun Bundle.provideChildArguments() {}

    override fun added(editor: Editor<*>, idx: Int) {
        val view = context.createControl(editor) { provideChildArguments() }
        val c = getCell(idx, view)
        cells.drop(idx).forEach { cell -> cell.index = cell.index + 1 }
        cells.add(idx, c)
        items.children.add(idx, c)
        addChild(view, idx)
        c.requestFocus()
    }

    private fun getCell(idx: Int, control: EditorControl<*>): Cell<*> {
        control.root //Initialize root
        val nxt = cells.getOrNull(idx + 1)?.item
        if (nxt != null) {
            control.setNext(nxt)
            nxt.setPrevious(control)
        }
        val prev = cells.getOrNull(idx - 1)?.item
        if (prev != null) {
            control.setPrevious(prev)
            prev.setNext(control)
        }
        control.setEditorParent(this)
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
        val ORIENTATION = SimpleProperty<Orientation>("list view orientation")

        /**
         * The [ListEditorControl.cellFactory] used to display items
         */
        val CELL_FACTORY = SimpleProperty.withDefault<() -> Cell<*>>("list view cell factory") { DefaultCell() }

        /**
         * The [Node] that is displayed when no items are in the [ListEditor]
         */
        val EMPTY_DISPLAY = SimpleProperty.withDefault<() -> Node>("empty display") { Glyphs.create(PLUS) }
    }
}