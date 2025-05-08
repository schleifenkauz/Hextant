/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.*
import fxutils.*
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.context.SelectionDistributor
import hextant.context.createControl
import hextant.core.Editor
import hextant.core.editor.ListEditor
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.CONTROL_DOWN
import javafx.scene.input.KeyCombination.SHIFT_DOWN
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import kotlinx.serialization.Serializable
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

    private val cells = mutableListOf<Cell<*>>()

    private var layout = orientation.createLayout()
        set(value) {
            field = value
            if (editor.editors.now.isNotEmpty()) root = value
        }

    private fun orientationChanged(new: Orientation) {
        layout = new.createLayout()
        layout.children.addAll(cells)
    }

    private fun cellFactoryChanged() {
        cells.clear()
        cells.addAll(editorChildren().mapIndexed { idx, control -> getCell(idx, control) })
        layout.children.setAll(cells)
    }

    init {
        initEmptyDisplay()
        editor.addView(this)
        addArgumentHandler(ORIENTATION, ::orientationChanged)
        addArgumentHandler(CELL_FACTORY) { cellFactoryChanged() }
        registerShortcuts {
            on("Ctrl+DELETE") { removeSelected() }
        }
    }

    private fun removeSelected() {
        val selected = getSelectedChildren()
        for (child in selected) {
            @Suppress("UNCHECKED_CAST")
            editor as ListEditor<*, Editor<*>>
            editor.remove(child.target)
        }
    }

    private fun initEmptyDisplay() {
        emptyDisplay?.setOnMouseClicked {
            editor.addAt(0)
        }
        registerShortcuts {
            on(PASTE_MANY) { editor.pasteManyFromClipboard(0) }
        }
        emptyDisplay?.registerShortcuts {
            on(ADD_ITEM_AFTER) { editor.addAt(0) }
        }
    }

    override fun createDefaultRoot(): Pane = layout

    /**
     * This method may be overwritten to pass arguments to children of this list editor view
     */
    protected open fun BundleBuilder.provideChildArguments() {}

    override fun added(editor: Editor<*>, idx: Int) {
        val view = context.createControl(editor) { provideChildArguments() }
        view.initializeControl()
        val c = getCell(idx, view)
        cells.drop(idx).forEach { cell -> cell.index += 1 }
        cells.add(idx, c)
        if (orientation is Orientation.Flexible) {
            val lines = getLines()
            var lineIdx = 0
            var i = 0
            while (i < idx) {
                i += lines[i].children.size
                lineIdx++
            }

        } else {
            layout.children.add(idx, c)
        }
        addChild(view, idx)
        if (scene != null) c.requestFocus()
    }

    private fun getCell(idx: Int, control: EditorControl<*>): Cell<*> {
        return cellFactory().apply {
            item = control
            index = idx
            initEventHandlers(this)
        }
    }

    private fun initEventHandlers(cell: Cell<*>) {
        cell.registerShortcuts {
            this.on(ADD_ITEM_AFTER) { editor.addAt(cell.index + 1) }
            this.on(ADD_ITEM_BEFORE) { editor.addAt(cell.index) }
            this.on(PASTE_MANY) { editor.pasteManyFromClipboard(cell.index) }
            if (orientation is Orientation.Flexible) {
                this.on("Shift+Enter") {
                    setLineBreak(cell, true)
                }
                this.on("Shift+BACKSPACE") {
                    setLineBreak(cell, false)
                }
            }
        }
        cell.addEventHandler(KeyEvent.KEY_PRESSED) { ev ->
            val prevKey = when (this.arguments[ORIENTATION]) {
                Orientation.Horizontal, is Orientation.Flexible -> KeyCode.LEFT
                Orientation.Vertical -> KeyCode.UP
            }
            val nextKey = when (this.arguments[ORIENTATION]) {
                Orientation.Vertical -> KeyCode.DOWN
                Orientation.Horizontal, is Orientation.Flexible -> KeyCode.RIGHT
            }
            when {
                KeyCodeCombination(prevKey, CONTROL_DOWN).match(ev) -> this.editor.swap(cell.index, cell.index - 1)
                KeyCodeCombination(nextKey, CONTROL_DOWN).match(ev) -> this.editor.swap(cell.index, cell.index + 1)
                KeyCodeCombination(prevKey, CONTROL_DOWN, SHIFT_DOWN).match(ev) ->
                    this.extendListSelection(cell.index, -1)

                KeyCodeCombination(nextKey, CONTROL_DOWN, SHIFT_DOWN).match(ev) ->
                    this.extendListSelection(cell.index, +1)

                else -> return@addEventHandler
            }
            ev.consume()
        }
        cell.addEditorButton?.setOnAction {
            editor.addAt(cell.index + 1)
        }
    }

    override fun removed(idx: Int) {
        val cell = cells.removeAt(idx)
        cells.drop(idx).forEach { c -> c.index -= 1 }
        removeChild(idx)
        val orientation = orientation
        if (orientation is Orientation.Flexible) {
            val lines = getLines()
            val line = lines.find { line -> cell in line.children }!!
            line.children.remove(cell)
            if (line.children.isEmpty()) {
                layout.children.remove(line)
            }
        } else {
            layout.children.removeAt(idx)

        }
        if (idx == 0 && cells.isNotEmpty()) cells[0].requestFocus()
        else if (idx != 0) cells[idx - 1].requestFocus()
    }

    override fun swapped(i: Int, j: Int) {
        swapChildren(i, j)
        val tmp = cells[i]
        cells[i] = cells[j]
        cells[j] = tmp
        cells[i].index = i
        cells[j].index = j
        context[SelectionDistributor].saveSelectionState()
        layout.children[j] = Region() //avoid duplicate children
        layout.children[i] = cells[i]
        layout.children[j] = cells[j]
        context[SelectionDistributor].restoreSelectionState()
    }

    private fun setLineBreak(cell: Cell<*>, lineBreak: Boolean) {
        val data = orientation as Orientation.Flexible
        data.lineBreaks[cell.index] = lineBreak
        val lines = getLines()
        val line = lines.find { line -> cell in line.children }
        if (line == null) {
            System.err.println("Line for cell at index ${cell.index} not found")
            return
        }
        val lineIdx = layout.children.indexOf(line)
        val idxInLine = line.children.indexOf(cell)
        if (lineBreak) {
            if (cell == line.children.last()) {
                editor.addAt(cell.index + 1)
            }
            val childrenBefore = line.children.take(idxInLine + 1).toTypedArray()
            val childrenAfter = line.children.drop(idxInLine + 1).toTypedArray()
            layout.children.removeAt(lineIdx)
            val splitLines = listOf(HBox(*childrenBefore), HBox(*childrenAfter))
            layout.children.addAll(lineIdx, splitLines)
        } else {
            if (lineIdx == 0) return
            if (idxInLine != 0) return //necessary?
            val lineBefore = lines[lineIdx - 1]
            layout.children.remove(line)
            lineBefore.children.addAll(line.children)
        }
    }

    private fun getLines() = layout.children.map { line -> line as HBox }

    private fun extendListSelection(index: Int, delta: Int) {
        val selectedViews = getSelectedChildren()
        selectedViews.sortBy { v -> editorChildren().indexOf(v) }
        val sourceView = editorChildren()[index]
        if (selectedViews.isEmpty()) {
            sourceView.select()
            return
        }
        if (selectedViews == listOf(sourceView)) {
            editorChildren().getOrNull(index + delta)?.toggleSelection()
            return
        }
        val selector = context[SelectionDistributor]
        when (sourceView) {
            selectedViews.last() -> {
                var i = index
                for (v in selectedViews.dropLast(1).reversed()) { //.reversed() copies, so no concurrent modification
                    val j = editorChildren().indexOf(v)
                    if (j != i - 1) {
                        selectedViews.remove(v)
                        i = Int.MAX_VALUE
                    }
                }
                when (delta) {
                    -1 -> {
                        selector.toggleSelection(sourceView)
                        editorChildren().getOrNull(index - 1)?.focus()
                    }

                    1 -> {
                        editorChildren().getOrNull(index + 1)?.toggleSelection()
                    }
                }
            }

            selectedViews.first() -> {
                var i = index
                for (v in selectedViews.drop(1)) {
                    val j = editorChildren().indexOf(v)
                    if (j != i + 1) {
                        selectedViews.remove(v)
                        i = Int.MIN_VALUE
                    }
                }
                when (delta) {
                    1 -> {
                        selector.toggleSelection(sourceView)
                        editorChildren().getOrNull(index + 1)?.focus()
                    }

                    -1 -> {
                        editorChildren().getOrNull(index - 1)?.toggleSelection()
                    }
                }
            }

            else -> sourceView.select()
        }
    }

    private fun getSelectedChildren(): MutableList<EditorControl<*>> {
        val selector = context[SelectionDistributor]
        val selectedViews = selector.selectedViews.now.filterIsInstance<EditorControl<*>>().toMutableList()
        val itr = selectedViews.listIterator()
        for (v in itr) {
            if (v !in editorChildren()) {
                val parent = v.editorParent
                selector.toggleSelection(v)
                if (parent is WrappingEditorControl<*> && parent in editorChildren()) {
                    itr.set(parent)
                    selector.toggleSelection(parent)
                } else {
                    itr.remove()
                }
            }
        }
        return selectedViews
    }

    override fun empty() {
        val focused = root.isFocusWithin
        layout.children.clear()
        cells.clear()
        root = emptyDisplay ?: layout
        if (focused) requestFocus()
    }

    override fun notEmpty() {
        root = layout
    }

    override fun receiveFocus() {
        val firstChild = cells.firstOrNull() ?: emptyDisplay
        firstChild?.requestFocus()
    }

    /**
     * Decides whether items are displayed horizontally or vertically in a [ListEditorControl]
     */
    @Serializable
    sealed class Orientation {

        internal fun createLayout(): Pane = when (this) {
            Horizontal -> HBox()
            Vertical -> VBox()
            is Flexible -> VBox()
        }

        data object Horizontal : Orientation()
        data object Vertical : Orientation()
        data class Flexible(val lineBreaks: MutableList<Boolean>) : Orientation()
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

        open val addEditorButton: Button? = null

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
        final override val addEditorButton: Button? = null,
        private val displayItem: (control: EditorControl<*>) -> Node = { it }
    ) : Cell<HBox>() {
        private val indexLabel = Label().withStyleClass("editor-list-number")

        init {
            root = HBox(indexLabel, Region()).withStyleClass("numbered-cell")
            if (addEditorButton != null) {
                root.children.add(addEditorButton)
            }
        }

        override fun updateIndex(idx: Int) {
            indexLabel.text = "${idx + startIndex}."
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
            root = HBox(Region(), Region()).centerChildren()
        }

        override fun updateIndex(idx: Int) {
            root.children[0] = if (idx != 0) separator else Region()
        }

        override fun updateItem(item: EditorControl<*>) {
            root.children[1] = displayItem(item)
        }
    }

    companion object {
        private val ADD_ITEM_AFTER = shortcut(KeyCode.INSERT) { control(ModifierValue.MAYBE) }

        private val ADD_ITEM_BEFORE =
            shortcut(KeyCode.INSERT) { shift(ModifierValue.DOWN); control(ModifierValue.MAYBE) }

        private val REMOVE_ITEM = shortcut(KeyCode.DELETE) { control(ModifierValue.MAYBE) }

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
        val EMPTY_DISPLAY = publicProperty<() -> Node?>("empty display") {
            Glyphs.create(PLUS).withStyleClass("standard-empty-display")
        }

        val ADD_WITH_COMMA = publicProperty<Boolean>("Add with comma", false)
    }
}