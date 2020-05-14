/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.fx

import bundles.Bundle
import bundles.createBundle
import hextant.Editor
import hextant.fx.CompoundEditorControl.Compound
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.ContentDisplay.RIGHT
import javafx.scene.input.*
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.layout.Region
import javafx.stage.PopupWindow

internal fun control(skin: Skin<out Control>): Control {
    return object : Control() {
        init {
            setSkin(skin)
        }
    }
}

internal fun Control.setRoot(node: Node) {
    skin = null
    skin = skin(this, node)
}

internal fun skin(control: Control, node: Node): Skin<Control> = SimpleSkin(control, node)


private class SimpleSkin(
    private val control: Control, private val node: Node
) : Skin<Control> {
    override fun getSkinnable(): Control = control

    override fun getNode(): Node = node

    override fun dispose() {}
}

internal inline fun Node.registerShortcut(s: KeyCombination, crossinline action: () -> Unit) {
    addEventHandler(KeyEvent.KEY_RELEASED) { k ->
        if (s.match(k)) {
            action()
            k.consume()
        }
    }
}

internal fun PopupWindow.show(node: Node) {
    val p = node.localToScreen(0.0, node.prefHeight(-1.0)) ?: return
    show(node, p.x, p.y)
}

internal fun TextField.smartSetText(new: String) {
    val previous = text
    if (previous != new) {
        text = new
        if (previous.isEmpty()) {
            positionCaret(new.length)
        }
    }
}

internal fun Node.onAction(action: () -> Unit) {
    addEventHandler(KeyEvent.KEY_RELEASED) { ev ->
        if (ev.code == ENTER) {
            action()
            ev.consume()
        }
    }
    addEventHandler(MouseEvent.MOUSE_PRESSED) { ev ->
        if (ev.clickCount >= 2) {
            action()
            ev.consume()
        }
    }
}

/**
 * Return a [Label] with the given text and with the 'hextant-text' and the 'keyword' style class.
 */
fun keyword(name: String) = Label(name).apply {
    styleClass.add("hextant-text")
    styleClass.add("keyword")
}

/**
 * Return a [Label] with the given text and with the 'hextant-text' and the 'operator' style class.
 */
fun operator(name: String) = Label(name).apply {
    styleClass.add("hextant-text")
    styleClass.add("operator")
}

internal fun Region.fixWidth(value: Double) {
    prefWidth = value
    minWidth = value
    maxWidth = value
}

internal fun <N : Node> N.withStyleClass(vararg names: String) = apply { styleClass.addAll(*names) }

internal fun <C : Control> C.withTooltip(tooltip: Tooltip) = apply { this.tooltip = tooltip }

internal fun <C : Control> C.withTooltip(text: String) = withTooltip(Tooltip(text))

internal fun label(text: String, graphic: Node? = null) = Label(text, graphic).withStyleClass("hextant-text")

internal fun Label.graphicToRight() = apply { contentDisplay = RIGHT }

/**
 * Add the editor control for the given [editor] to this compound view.
 * The [config] block is used to initialize properties of the [hextant.EditorView.arguments] bundle.
 */
fun Compound.view(editor: Editor<*>, bundle: Bundle = createBundle(), config: Bundle.() -> Unit) {
    view(editor, bundle.apply(config))
}