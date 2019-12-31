/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.fx

import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.input.KeyCode.ENTER
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

internal
fun skin(control: Control, node: Node): Skin<Control> = SimpleSkin(control, node)


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

fun keyword(name: String) = Label(name).apply {
    styleClass.add("hextant-text")
    styleClass.add("keyword")
}

fun operator(name: String) = Label(name).apply {
    styleClass.add("hextant-text")
    styleClass.add("operator")
}