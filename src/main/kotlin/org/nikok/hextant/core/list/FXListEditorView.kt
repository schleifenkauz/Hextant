/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.list

import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.input.*
import javafx.scene.layout.*
import org.nikok.hextant.*
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.fx.setRoot
import org.nikok.hextant.core.list.FXListEditorView.Orientation.Vertical

class FXListEditorView(
    private val editable: EditableList<*, *>,
    private val editor: ListEditor<*>,
    private val context: Context,
    private val emptyDisplay: Node,
    orientation: Orientation = Vertical
) : ListEditorView,
    EditorControl<Node>() {
    private var items = orientation.createLayout()

    var orientation = orientation
        set(new) {
            field = new
            orientationChanged(new)
        }

    constructor(
        editable: EditableList<*, *>,
        editor: ListEditor<*>,
        context: Context,
        orientation: Orientation = Vertical,
        emptyText: String = "Add item"
    ) : this(editable, editor, context, Button(emptyText), orientation)

    private fun orientationChanged(new: Orientation) {
        items = new.createLayout()
        addChildren()
    }

    private fun addChildren() {
        for (c in cells) {
            items.children.add(c)
        }
    }

    var cellFactory: () -> Cell<*> = { DefaultCell() }
        set(value) {
            field = value
            cellFactoryChanged()
        }

    private fun cellFactoryChanged() {
        cells.clear()
        cells.addAll(cells(editable.editableList.now))
        items.children.clear()
        addChildren()
    }

    private val cells = mutableListOf<Cell<*>>()

    private fun cells(items: List<Editable<*>>) =
        items.mapIndexedTo(mutableListOf()) { idx, e -> getCell(idx, e) }

    private class DefaultCell : Cell<EditorControl<*>>() {
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

        override fun requestFocus() {
            item?.requestFocus()
        }
    }

    class PrefixCell(prefix: Node) : Cell<HBox>() {
        constructor(text: String) : this(Label(text))

        init {
            root = HBox(prefix)
        }

        override fun requestFocus() {
            item?.requestFocus()
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

        override fun requestFocus() {
            item?.requestFocus()
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
            root.requestFocus()
        }

        protected open fun updateIndex(idx: Int) {}

        protected open fun updateItem(item: EditorControl<*>) {}
    }

    init {
        initialize(editable, editor, context)
        editor.addView(this)
        initEmptyDisplay()
    }

    private fun initEmptyDisplay() {
        emptyDisplay.setOnMouseClicked {
            editor.add(0)
        }
        emptyDisplay.setOnKeyReleased { evt ->
            if (ADD_ITEM.match(evt)) {
                editor.add(0)
                evt.consume()
            }
        }
    }

    override fun createDefaultRoot(): Pane = items

    override fun added(editable: Editable<*>, idx: Int) {
        val c = getCell(idx, editable)
        cells.drop(idx).forEach { cell -> cell.index = cell.index + 1 }
        cells.add(idx, c)
        items.children.add(idx, c)
        c.requestFocus()
    }

    private fun getCell(idx: Int, editable: Editable<*>): Cell<*> {
        val control = context.createView(editable)
        return cellFactory().apply {
            item = control
            index = idx
            initEventHandlers()
        }
    }

    private fun Cell<*>.initEventHandlers() {
        addEventHandler(KeyEvent.KEY_RELEASED) { evt ->
            when {
                ADD_ITEM.match(evt)                        -> addNew(evt)
                REMOVE_ITEM.match(evt)                     -> removeCurrent(evt)
                orientation.nextCombination.match(evt)     -> focusNext(evt)
                orientation.previousCombination.match(evt) -> focusPrevious(evt)
            }
        }
    }

    private fun Cell<*>.addNew(evt: KeyEvent) {
        editor.add(index + 1)
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
    }
}