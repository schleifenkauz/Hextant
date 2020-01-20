/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.fx.ModifierValue.*
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.input.*
import java.text.ParseException

enum class ModifierValue { DOWN, UP, MAYBE }

fun ModifierValue.fx(): KeyCombination.ModifierValue = when (this) {
    DOWN  -> KeyCombination.ModifierValue.DOWN
    UP    -> KeyCombination.ModifierValue.UP
    MAYBE -> KeyCombination.ModifierValue.ANY
}

data class Shortcut(val key: KeyCode?, val control: ModifierValue, val alt: ModifierValue, val shift: ModifierValue) {
    override fun toString(): String = if (key == null) "Never" else buildString {
        for ((name, value) in listOf("Ctrl" to control, "Alt" to alt, "Shift" to shift)) {
            when (value) {
                DOWN  -> append("$name + ")
                UP    -> {
                }
                MAYBE -> append("$name? + ")
            }
        }
        append(key.name)
    }

    fun matches(ev: KeyEvent): Boolean = when {
        ev.code != key                       -> false
        ev.isAltDown && alt == UP            -> false
        !ev.isAltDown && alt == DOWN         -> false
        ev.isControlDown && control == UP    -> false
        !ev.isControlDown && control == DOWN -> false
        ev.isShiftDown && shift == UP        -> false
        !ev.isShiftDown && shift == DOWN     -> false
        else                                 -> true
    }

    fun toCombination(): KeyCombination = KeyCodeCombination(key, shift.fx(), control.fx(), alt.fx(), UP.fx(), UP.fx())
}

class ShortcutBuilder @PublishedApi internal constructor(private val key: KeyCode) {
    private var control = UP
    private var alt = UP
    private var shift = UP

    fun control(v: ModifierValue) {
        control = v
    }

    fun alt(v: ModifierValue) {
        alt = v
    }

    fun shift(v: ModifierValue) {
        shift = v
    }

    fun build() = Shortcut(key, control, alt, shift)
}

inline fun shortcut(key: KeyCode, block: ShortcutBuilder.() -> Unit = {}) = ShortcutBuilder(key).apply(block).build()

fun never() = Shortcut(null, UP, UP, UP)

class KeyEventHandlerBody(private val event: KeyEvent) {
    fun on(shortcut: Shortcut, consume: Boolean = true, action: (KeyEvent) -> Unit) {
        if (shortcut.matches(event)) {
            action(event)
            if (consume) event.consume()
        }
    }

    fun on(
        code: KeyCode,
        builder: ShortcutBuilder.() -> Unit = {},
        consume: Boolean = true,
        action: (KeyEvent) -> Unit
    ) {
        val shortcut = ShortcutBuilder(code).apply(builder).build()
        on(shortcut, consume, action)
    }

    fun on(shortcut: String, consume: Boolean = true, action: (KeyEvent) -> Unit) {
        on(shortcut.shortcut, consume, action)
    }
}

val String.shortcut get() = parseShortcut(this)

fun checkNotSet(value: ModifierValue, idx: Int, tok: String) {
    if (value != UP) throw ParseException("Duplicate modifier $tok", idx)
}

fun parseShortcut(s: String): Shortcut {
    var code: KeyCode? = null
    var ctrl = UP
    var shift = UP
    var alt = UP
    val builder = StringBuilder()
    fun tokenComplete(idx: Int) {
        when (val tok = builder.toString().toUpperCase()) {
            "CTRL"   -> {
                checkNotSet(ctrl, idx, tok)
                ctrl = DOWN
            }
            "CTRL?"  -> {
                checkNotSet(ctrl, idx, tok)
                ctrl = MAYBE
            }
            "SHIFT"  -> {
                checkNotSet(shift, idx, tok)
                shift = DOWN
            }
            "SHIFT?" -> {
                checkNotSet(shift, idx, tok)
                shift = MAYBE
            }
            "ALT"    -> {
                checkNotSet(alt, idx, tok)
                alt = DOWN
            }
            "ALT?"   -> {
                checkNotSet(alt, idx, tok)
                alt = MAYBE
            }
            else     -> try {
                code = KeyCode.valueOf(tok)
            } catch (e: IllegalArgumentException) {
                throw ParseException("Illegal token '$tok'", idx)
            }
        }
        builder.clear()
    }
    for ((idx, c) in s.withIndex()) {
        when (c) {
            '+'  -> tokenComplete(idx)
            ' '  -> {
            }
            else -> builder.append(c)
        }
    }
    tokenComplete(s.length - 1)
    if (code == null) throw ParseException("Illegal shortcut, has no main key", 0)
    return Shortcut(code, ctrl, alt, shift)
}

fun Node.registerShortcuts(
    eventType: EventType<KeyEvent> = KeyEvent.KEY_RELEASED,
    handle: KeyEventHandlerBody.() -> Unit
) {
    addEventHandler(eventType) { ev: KeyEvent ->
        KeyEventHandlerBody(ev).handle()
    }
}