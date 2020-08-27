/**
 * @author Nikolaus Knop
 */

package hextant.fx

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color


/**
 * Return a [MenuBar] configured with the given [builder].
 */
inline fun menuBar(builder: MenuBarBuilder.() -> Unit) = MenuBarBuilder().apply(builder).build()

/**
 * Builder class for [MenuBar]s
 */
class MenuBarBuilder @PublishedApi internal constructor() {
    private val menus = mutableListOf<Menu>()

    /**
     * Add the given [menu]
     */
    fun menu(menu: Menu) {
        menus.add(menu)
    }

    /**
     * Add a menu with the given [name] and configure it with the given [block].
     */
    inline fun menu(name: String, block: MenuBuilder.() -> Unit) {
        menu(MenuBuilder(name).apply(block).build())
    }

    @PublishedApi internal fun build() = MenuBar(*menus.toTypedArray())
}

/**
 * Builder class for [Menu]s
 */
class MenuBuilder @PublishedApi internal constructor(private val name: String) {
    private var items = mutableListOf<MenuItem>()

    /**
     * Add an item with the specified [name] and [shortcut], which executes the given [action] when clicked.
     */
    fun item(name: String, shortcut: Shortcut? = null, action: () -> Unit) {
        val item = MenuItem(name)
        item.accelerator = shortcut?.toCombination()
        item.setOnAction { action() }
        items.add(item)
    }

    /**
     * Add an item with the specified [name] and [shortcut], which executes the given [action] when clicked.
     */
    fun item(name: String, shortcut: String, action: () -> Unit) {
        item(name, shortcut.shortcut, action)
    }

    @PublishedApi internal fun build() = Menu(name, null, *items.toTypedArray())
}

/**
 * Create a [Button] and apply the given [block] to it.
 */
inline fun button(text: String = "", graphic: Node? = null, block: Button.() -> Unit = {}): Button =
    Button(text, graphic).apply(block)

/**
 * Create a [Label] with the given [text] and apply the given [block] to it.
 */
inline fun label(text: String = "", graphic: Node? = null, block: Label.() -> Unit = {}): Label =
    Label(text, graphic).apply(block)

/**
 * Create a [TextField] with the given [text] and apply the given [block] to it.
 */
inline fun textField(text: String = "", block: TextField.() -> Unit = {}): TextField =
    TextField(text).apply(block)

/**
 * Create a [Background] with the specified [color].
 */
fun Region.setBackground(color: Color, cornerRadii: CornerRadii? = CornerRadii.EMPTY, insets: Insets = this.insets) {
    background = Background(BackgroundFill(color, cornerRadii, insets))
}

/**
 * Set the [Insets.left], [Insets.right], [Insets.top] and [Insets.bottom] values of the padding to the given [value].
 */
fun Region.setPadding(value: Double) {
    padding = Insets(value)
}

/**
 * Create a [HBox] with the given [children] and apply the given [block] to it.
 */
inline fun hbox(vararg children: Node, block: HBox.() -> Unit = {}): HBox = HBox(*children).apply(block)

/**
 * Create a [VBox] with the given [children] and apply the given [block] to it.
 */
inline fun vbox(vararg children: Node, block: VBox.() -> Unit = {}): VBox = VBox(*children).apply(block)

/**
 * Create a [BorderPane] and apply the given [block] to it.
 */
inline fun borderPane(block: BorderPane.() -> Unit = {}): BorderPane = BorderPane().apply(block)

/**
 * Used to add nodes to the children of JavaFX layout elements.
 */
class ChildrenAdder @PublishedApi internal constructor(private val children: MutableList<Node>) {
    /**
     * Add this node to the children.
     */
    operator fun Node.unaryPlus() {
        children.add(this)
    }
}

/**
 * Add children to this [Pane].
 */
inline fun Pane.children(block: ChildrenAdder.() -> Unit) {
    ChildrenAdder(children).block()
}

/**
 * Adds the given [node] to the [Pane.children] of this [Pane].
 */
inline fun <N : Node> Pane.add(node: N, block: N.() -> Unit = {}) {
    node.block()
    children.add(node)
}

/**
 * Sets the [Labeled.alignment] to [Pos.CENTER].
 */
fun Labeled.center() {
    alignment = Pos.CENTER
}