/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core.fx

import javafx.event.Event
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.input.KeyCode.*
import javafx.stage.PopupWindow
import org.nikok.hextant.Editor
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.command.gui.commandContextMenu
import org.nikok.hextant.core.inspect.Inspections
import org.nikok.hextant.core.inspect.gui.InspectionPopup

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

private fun skin(control: Control, node: Node): Skin<Control> = SimpleSkin(control, node)


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

private val ALT_LEFT = KeyCodeCombination(LEFT, KeyCombination.ALT_DOWN)

private val ALT_RIGHT = KeyCodeCombination(RIGHT, KeyCombination.ALT_DOWN)

internal fun TextField.setLeft(node: () -> Node) {
    addEventHandler(KeyEvent.KEY_RELEASED) { k ->
        if (ALT_LEFT.match(k) || k.code == LEFT && caretPosition == 0) {
            node().requestFocus()
            k.consume()
        }
    }
}

internal fun TextField.setRight(node: () -> Node) {
    addEventHandler(KeyEvent.KEY_RELEASED) { k ->
        if (ALT_RIGHT.match(k) || k.code == RIGHT && caretPosition == 0) {
            node().requestFocus()
            k.consume()
        }
    }
}

internal fun Node.setUp(node: () -> Node) {
    registerShortcut(KeyCodeCombination(UP)) { node().requestFocus() }
}

internal fun Node.setDown(node: () -> Node) {
    registerShortcut(KeyCodeCombination(DOWN)) { node().requestFocus() }
}

internal fun PopupWindow.show(node: Node) {
    val p = node.localToScreen(0.0, node.prefHeight(-1.0))
    show(node, p.x, p.y)
}

internal fun Node.focusNext() {
    val event = KeyEvent(
        this,
        this,
        KeyEvent.KEY_RELEASED,
        "\t",
        "TAB",
        KeyCode.TAB,
        false,
        false,
        false,
        false
    )
    Event.fireEvent(this, event)
}

internal fun Node.focusPrevious() {
    val event = KeyEvent(
        this,
        this,
        KeyEvent.KEY_RELEASED,
        "\t",
        "TAB",
        KeyCode.TAB,
        true,
        false,
        false,
        false
    )
    Event.fireEvent(this, event)
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

internal fun Node.activateInspections(inspected: Any) {
    val inspections = HextantPlatform[Public, Inspections]
    val p = InspectionPopup(this) { inspections.getProblems(inspected) }
    registerShortcut(KeyCodeCombination(ENTER, KeyCombination.ALT_DOWN)) { p.show(this) }
}

internal fun <T: Any> Node.activateContextMenu(target: T) {
    val commands = HextantPlatform[Public, Commands]
    val registrar = commands.of(target::class)
    val contextMenu = target.commandContextMenu(registrar)
    setOnContextMenuRequested { contextMenu.show(this, Side.BOTTOM, 0.0, 0.0) }
}