/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.list

import javafx.scene.Node
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.input.*
import javafx.scene.layout.*
import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.fx.setRoot
import org.nikok.hextant.core.list.FXListEditorView.Orientation.Vertical

class FXListEditorView(
    private val editable: EditableList<*, *>,
    private val editor: ListEditor<*>,
    orientation: Orientation = Vertical,
    platform: HextantPlatform
) : ListEditorView,
    EditorControl<Pane>() {
    var orientation = orientation
        set(new) {
            field = new
            orientationChanged(new)
        }

    private fun orientationChanged(new: Orientation) {
        root = new.createLayout()
        addChildren()
    }

    private fun addChildren() {
        root.children.clear()
        for (c in cells) {
            root.children.add(c)
        }
    }

    var cellFactory: () -> Cell<*> = { DefaultCell() }
        set(value) {
            field = value
            cells.clear()
            cells.addAll(cells(editable.editableList.now))
            addChildren()
        }

    private val viewFactory = platform[Public, EditorViewFactory]

    private val cells = cells(editable.editableList.now)

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
            root = HBox(prefix, null)
        }

        override fun requestFocus() {
            item?.requestFocus()
        }

        override fun updateItem(item: EditorControl<*>) {
            root.children[1] = item
        }
    }

    class SeparatorCell(private val separator: Node) : Cell<HBox>() {
        constructor(text: String) : this(Label(text))

        init {
            root = HBox()
        }

        override fun requestFocus() {
            item?.requestFocus()
        }

        override fun updateIndex(idx: Int) {
            if (idx == 0) root.children.add(0, separator)
            else root.children.removeAt(0)
        }

        override fun updateItem(item: EditorControl<*>) {
            val children = root.children
            if (item in children) children[children.size - 1] = item
            else children.add(item)
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
        initialize(editable, editor, platform)
        for (c in cells) {
            root.children.add(c)
        }
        editor.addView(this)
    }

    override fun createDefaultRoot(): Pane = orientation.createLayout()

    override fun added(editable: Editable<*>, idx: Int) {
        val c = getCell(idx, editable)
        cells.drop(idx).forEach { cell -> cell.index = cell.index + 1 }
        cells.add(idx, c)
        root.children.add(idx, c)
        c.requestFocus()
    }

    private fun getCell(idx: Int, editable: Editable<*>): Cell<*> {
        val control = viewFactory.getFXView(editable)
        return cellFactory().apply {
            item = control
            index = idx
            addEventHandler(KeyEvent.KEY_RELEASED) { evt ->
                when {
                    ADD_ITEM.match(evt)                        -> {
                        editor.add(index + 1)
                        evt.consume()
                    }
                    REMOVE_ITEM.match(evt)                     -> {
                        editor.removeAt(index)
                        evt.consume()
                    }
                    orientation.nextCombination.match(evt)     -> {
                        if (cells.size > index + 1) {
                            cells[index + 1].requestFocus()
                            evt.consume()
                        }
                    }
                    orientation.previousCombination.match(evt) -> {
                        if (index > 0) {
                            cells[index - 1].requestFocus()
                            evt.consume()
                        }
                    }
                }
            }
        }
    }

    override fun removed(idx: Int) {
        root.children.removeAt(idx)
        cells.removeAt(idx)
        cells.drop(idx).forEach { c -> c.index = c.index - 1 }
        if (idx == 0 && cells.size > 0) cells[0].requestFocus()
        else cells[idx - 1].requestFocus()
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