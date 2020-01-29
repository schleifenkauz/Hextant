/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.Editor
import hextant.base.EditorControl
import hextant.bundle.*
import hextant.bundle.CorePermissions.Public
import hextant.core.editor.ListEditor
import hextant.createView
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

open class ListEditorControl(
    private val editor: ListEditor<*, *>,
    args: Bundle
) : ListEditorView, EditorControl<Node>(editor, args) {
    var orientation = arguments.getOrNull(Public, ORIENTATION) ?: Orientation.Vertical
        set(new) {
            field = new
            orientationChanged(new)
        }

    private val emptyDisplay = arguments[Public, EMPTY_DISPLAY]

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

    var cellFactory: () -> Cell<*> = arguments.getOrNull(Public, CELL_FACTORY) ?: { DefaultCell() }
        set(value) {
            field = value
            cellFactoryChanged()
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
            ORIENTATION  -> orientation = value as Orientation
            CELL_FACTORY -> cellFactory = value as () -> Cell<*>
        }
    }

    private val cells = mutableListOf<Cell<*>>()

    private fun cells(items: List<Editor<*>>) =
        items.mapIndexedTo(mutableListOf()) { idx, e -> getCell(idx, context.createView(e)) }

    class DefaultCell : Cell<EditorControl<*>>() {
        override fun updateItem(item: EditorControl<*>) {
            root = item
        }
    }

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

    abstract class Cell<R : Node> : Control() {
        var index: Int = -1
            internal set(value) {
                field = value
                updateIndex(value)
            }

        var item: EditorControl<*>? = null
            internal set(value) {
                value!!
                field = value
                updateItem(value)
            }

        private var _root: R? = null

        protected var root: R
            get() = _root ?: throw IllegalStateException("Root not initialized")
            set(value) {
                _root = value
                setRoot(value)
            }

        override fun requestFocus() {
            item?.receiveFocus()
        }

        protected open fun updateIndex(idx: Int) {}

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
        val view = context.createView(editor) { provideChildArguments() }
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
        root = emptyDisplay
    }

    override fun notEmpty() {
        root = items
    }

    override fun receiveFocus() {
        val firstChild = cells.firstOrNull() ?: emptyDisplay
        firstChild.requestFocus()
    }

    interface Orientation {
        fun createLayout(): Pane

        val nextCombination: Shortcut

        val previousCombination: Shortcut

        object Horizontal : Orientation {
            override fun createLayout(): Pane = HBox()

            override val nextCombination = "Right".shortcut

            override val previousCombination = "Left".shortcut
        }

        object Vertical : Orientation {
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

        fun withAltText(
            editor: ListEditor<*, *>,
            emptyText: String = "Add item",
            args: Bundle = Bundle.newInstance()
        ) = ListEditorControl(editor, args.also {
            it[Public, EMPTY_DISPLAY] = Button(emptyText)
        })

        fun withAltGlyph(
            editor: ListEditor<*, *>,
            glyph: FontAwesome.Glyph,
            args: Bundle = Bundle.newInstance(),
            orientation: Orientation = Orientation.Vertical
        ) = ListEditorControl(editor, args.also {
            it[Public, ORIENTATION] = orientation
            it[Public, EMPTY_DISPLAY] = Glyphs.create(glyph)
        })

        val ORIENTATION = Property<Orientation, Public, Public>("list view orientation")

        val CELL_FACTORY = Property<() -> Cell<*>, Public, Public>("list view cell factory")

        val EMPTY_DISPLAY = Property<Node, Public, Public>("empty display", Glyphs.create(PLUS))
    }
}