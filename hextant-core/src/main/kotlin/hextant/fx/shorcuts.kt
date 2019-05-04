/**
 * @author Nikolaus Knop
 */

package hextant.fx

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

data class Shortcut(val key: KeyCode, val modifiers: Set<KeyCode>) {
    constructor(key: KeyCode, vararg modifiers: KeyCode) : this(key, modifiers.toSet())
}

private fun KeyEvent.getShortcut(): Shortcut {
    val modifiers = mutableSetOf<KeyCode>()
    if (isControlDown) modifiers.add(KeyCode.CONTROL)
    if (isAltDown) modifiers.add(KeyCode.ALT)
    if (isMetaDown) modifiers.add(KeyCode.META)
    if (isShiftDown) modifiers.add(KeyCode.SHIFT)
    if (isShortcutDown) modifiers.add(KeyCode.CONTROL)
    return Shortcut(code, modifiers)
}

class ShortcutRegistrar : EventHandler<KeyEvent> {
    private val handlers = mutableMapOf<Shortcut, () -> Boolean>()

    override fun handle(event: KeyEvent) {
        val handler = handlers[event.getShortcut()] ?: return
        if (handler.invoke()) event.consume()
    }

    fun maybeOn(shortcut: Shortcut, handler: () -> Boolean) {
        handlers[shortcut] = handler
    }
}

inline fun ShortcutRegistrar.on(shortcut: Shortcut, crossinline handler: () -> Unit) {
    maybeOn(shortcut) { handler(); true }
}

inline fun Node.registerShortcuts(handlers: ShortcutRegistrar.() -> Unit) {
    val handler = ShortcutRegistrar()
    handler.handlers()
    addEventHandler(KeyEvent.KEY_RELEASED, handler)
}