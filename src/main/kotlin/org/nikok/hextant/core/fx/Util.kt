/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package org.nikok.hextant.core.fx

import com.sun.javafx.scene.traversal.Direction.NEXT
import com.sun.javafx.scene.traversal.Direction.PREVIOUS
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.*
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.input.KeyCode.W
import javafx.scene.input.KeyCombination.SHORTCUT_DOWN
import javafx.stage.PopupWindow
import org.nikok.hextant.*
import org.nikok.hextant.core.base.EditorControl
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

internal fun Node.activateInspections(inspected: Any, platform: HextantPlatform) {
    val inspections = platform[Inspections]
    val p = InspectionPopup(this) { inspections.getProblems(inspected) }
    registerShortcut(KeyCodeCombination(ENTER, KeyCombination.ALT_DOWN)) { p.show(this) }
}

internal fun <T: Any> Node.activateContextMenu(target: T, platform: HextantPlatform) {
    val contextMenu = target.commandContextMenu(platform)
    setOnContextMenuRequested { contextMenu.show(this, Side.BOTTOM, 0.0, 0.0) }
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

private val EXTEND_SELECTION = KeyCodeCombination(W, SHORTCUT_DOWN)

internal fun FXEditorView.activateSelectionExtension(editor: Editor<*>) {
    node.addEventHandler(KeyEvent.KEY_RELEASED) { k ->
        if (EXTEND_SELECTION.match(k) && !editor.isSelected) {
            editor.select()
            k.consume()
        }
    }
}

internal fun EditorControl<out Node>.activateSelectionExtension(editor: Editor<*>) {
    root.addEventHandler(KeyEvent.KEY_RELEASED) { k ->
        if (EXTEND_SELECTION.match(k) && !editor.isSelected) {
            editor.select()
            k.consume()
        }
    }
}