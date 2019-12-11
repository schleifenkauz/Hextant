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
import hextant.fx.Glyphs
import hextant.fx.setRoot
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.input.*
import javafx.scene.layout.*
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.FontAwesome.Glyph.PLUS

class FXListEditorView(
    private val editor: ListEditor<*, *>,
    args: Bundle,
    private val emptyDisplay: Node = Glyphs.create(PLUS)
) : ListEditorView, EditorControl<Node>(editor, args) {
    var orientation = arguments.getOrNull(Public, ORIENTATION) ?: Orientation.Vertical
        set(new) {
            field = new
            orientationChanged(new)
        }

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
        items.mapIndexedTo(mutableListOf()) { idx, e -> getCell(idx, e) }

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
        emptyDisplay.setOnKeyReleased { evt ->
            if (ADD_ITEM.match(evt)) {
                editor.addAt(0)
                evt.consume()
            }
        }
    }

    override fun createDefaultRoot(): Pane = items

    override fun added(editor: Editor<*>, idx: Int) {
        val c = getCell(idx, editor)
        cells.drop(idx).forEach { cell -> cell.index = cell.index + 1 }
        cells.add(idx, c)
        items.children.add(idx, c)
        c.requestFocus()
    }

    private fun getCell(idx: Int, editor: Editor<*>): Cell<*> {
        val control = context.createView(editor)
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
        addEventHandler(KeyEvent.KEY_RELEASED) { evt ->
            var consume = true
            when {
                ADD_ITEM.match(evt)                        -> addNew(evt)
                REMOVE_ITEM.match(evt)                     -> removeCurrent(evt)
                orientation.nextCombination.match(evt)     -> focusNext(evt)
                orientation.previousCombination.match(evt) -> focusPrevious(evt)
                else                                       -> consume = false
            }
            if (consume) evt.consume()
        }
    }

    private fun Cell<*>.addNew(evt: KeyEvent) {
        editor.addAt(index + 1)
        evt.consume()
    }

    private fun Cell<*>.focusPrevious(evt: KeyEvent) {
        if (index > 0) {
            cells[index - 1].requestFocus()
            evt.consume()
        }
    }

    private fun Cell<*>.focusNext(evt: KeyEvent) {
        if (cells.size > index + 1) {
            cells[index + 1].requestFocus()
            evt.consume()
        }
    }

    private fun Cell<*>.removeCurrent(evt: KeyEvent) {
        editor.removeAt(index)
        evt.consume()
    }

    override fun removed(idx: Int) {
        items.children.removeAt(idx)
        cells.removeAt(idx)
        cells.drop(idx).forEach { c -> c.index = c.index - 1 }
        if (idx == 0 && cells.size > 0) cells[0].requestFocus()
        else if (idx != 0) cells[idx - 1].requestFocus()
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

        val nextCombination: KeyCombination

        val previousCombination: KeyCombination

        object Horizontal : Orientation {
            override fun createLayout(): Pane = HBox()

            override val nextCombination: KeyCombination =
                KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHORTCUT_DOWN)

            override val previousCombination: KeyCombination =
                KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHORTCUT_DOWN)
        }

        object Vertical : Orientation {
            override fun createLayout(): Pane = VBox()

            override val nextCombination: KeyCombination = KeyCodeCombination(KeyCode.DOWN)

            override val previousCombination: KeyCombination = KeyCodeCombination(KeyCode.UP)
        }
    }

    companion object {
        private val ADD_ITEM = KeyCodeCombination(KeyCode.PLUS, KeyCombination.SHORTCUT_DOWN)

        private val REMOVE_ITEM = KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN)

        fun withAltText(
            editor: ListEditor<*, *>,
            emptyText: String = "Add item",
            args: Bundle = Bundle.newInstance()
        ) = FXListEditorView(editor, args, Button(emptyText))

        fun withAltGlyph(
            editor: ListEditor<*, *>,
            glyph: FontAwesome.Glyph,
            args: Bundle = Bundle.newInstance(),
            orientation: Orientation = Orientation.Vertical
        ) = FXListEditorView(editor, args.also {
            it[Public, ORIENTATION] = orientation
        }, Glyphs.create(glyph))

        val ORIENTATION = Property<Orientation, Public, Public>("list view orientation")

        val CELL_FACTORY = Property<() -> Cell<*>, Public, Public>("list view cell factory")
    }
}