/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core.fx

import com.sun.javafx.scene.traversal.Direction.NEXT
import com.sun.javafx.scene.traversal.Direction.PREVIOUS
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.stage.PopupWindow
import org.nikok.hextant.Editor

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
    val p = node.localToScreen(0.0, node.prefHeight(-1.0))
    show(node, p.x, p.y)
}

internal fun Node.focusNext() {
    impl_traverse(NEXT)
}

internal fun Node.focusPrevious() {
    impl_traverse(PREVIOUS)
}

internal fun Node.initSelection(editor: Editor<*>) {
    focusedProperty().addListener { _, _, isFocused ->
        if (isFocused) {
            if (isControlDown) {
                println("Focused $this, toggling selection of $editor")
                editor.toggleSelection()
            } else {
                println("Focused $this, selecting $editor")
                editor.select()
            }
        }
    }
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

fun keyword(name: String) = Label(name).also { it.styleClass.add("keyword") }

fun operator(name: String) = Label(name).also { it.styleClass.add("operator") }