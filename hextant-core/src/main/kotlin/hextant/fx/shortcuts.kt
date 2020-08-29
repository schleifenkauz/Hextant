/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.fx.ModifierValue.*
import javafx.event.EventType
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.*
import java.lang.ref.WeakReference
import java.text.ParseException

/**
 * Specifies whether a certain modifier key (e.g. shift) is up, down or it does not matter
 */
enum class ModifierValue {
    /**
     * The modifier **must** be pressed.
     */
    DOWN,

    /**
     * The modifier **must not** be pressed.
     */
    UP,

    /**
     * The modifier **may** be pressed.
     */
    MAYBE
}

/**
 * Map this [ModifierValue] to the JavaFX counterpart
 */
fun ModifierValue.fx(): KeyCombination.ModifierValue = when (this) {
    DOWN -> KeyCombination.ModifierValue.DOWN
    UP -> KeyCombination.ModifierValue.UP
    MAYBE -> KeyCombination.ModifierValue.ANY
}

/**
 * Represents a shortcut
 * @param key the main key, that the user pressed
 * @param control specifies whether the Ctrl modifier is pressed
 * @param alt specifies whether the Alt modifier is pressed
 * @param shift specifies whether the Shift modifier is pressed
 */
data class Shortcut(val key: KeyCode?, val control: ModifierValue, val alt: ModifierValue, val shift: ModifierValue) {
    override fun toString(): String = if (key == null) "Never" else buildString {
        for ((name, value) in listOf("Ctrl" to control, "Alt" to alt, "Shift" to shift)) {
            when (value) {
                DOWN -> append("$name + ")
                UP -> {
                }
                MAYBE -> append("$name? + ")
            }
        }
        append(key.name)
    }

    /**
     * Tests whether the given key event is recognized by this shortcut
     */
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

    /**
     * Map this shortcut to the JavaFX counterpart
     */
    fun toCombination(): KeyCombination = KeyCodeCombination(key, shift.fx(), control.fx(), alt.fx(), UP.fx(), UP.fx())
}

/**
 *
 */
class ShortcutBuilder @PublishedApi internal constructor(private val key: KeyCode) {
    private var control = UP
    private var alt = UP
    private var shift = UP

    /**
     * Set the modifier value for the control key
     */
    fun control(v: ModifierValue) {
        control = v
    }

    /**
     * Set the modifier value for the alt key
     */
    fun alt(v: ModifierValue) {
        alt = v
    }

    /**
     * Set the modifier value for the shift key
     */
    fun shift(v: ModifierValue) {
        shift = v
    }

    @PublishedApi internal fun build() = Shortcut(key, control, alt, shift)
}

/**
 * Return a shortcut with the given [key] and configure it with a [ShortcutBuilder].
 */
inline fun shortcut(key: KeyCode, block: ShortcutBuilder.() -> Unit = {}) = ShortcutBuilder(key).apply(block).build()

/**
 * Return a [Shortcut] the never matches any key event.
 */
fun never() = Shortcut(null, UP, UP, UP)

/**
 * Used to configure shortcuts.
 * @param receiver the context in which the actions should be executed
 */
class KeyEventHandlerBody<out R> @PublishedApi internal constructor(val receiver: R, private val event: KeyEvent) {
    /**
     * If the key event matches the given [shortcut], the given [action] is executed.
     * @param consume if `false` the user is responsible for consuming the key event
     */
    fun on(shortcut: Shortcut, consume: Boolean = true, action: R.(KeyEvent) -> Unit) {
        if (event.isConsumed) return
        if (shortcut.matches(event)) {
            receiver.action(event)
            if (consume) event.consume()
        }
    }

    /**
     * If the key event matches the given [shortcut], the given [action] is executed.
     * @param consume if `false` the user is responsible for consuming the key event
     */
    fun on(
        code: KeyCode,
        builder: ShortcutBuilder.() -> Unit = {},
        consume: Boolean = true,
        action: R.(KeyEvent) -> Unit
    ) {
        val shortcut = ShortcutBuilder(code).apply(builder).build()
        on(shortcut, consume, action)
    }

    /**
     * If the key event matches the given [shortcut], the given [action] is executed.
     * @param consume if `false` the user is responsible for consuming the key event
     */
    fun on(shortcut: String, consume: Boolean = true, action: R.(KeyEvent) -> Unit) {
        on(shortcut.shortcut, consume, action)
    }
}

/**
 * Parse a [Shortcut] from this string.
 * Examples:
 *   - `"A"`
 *   - `"Ctrl + A"`
 *   - `"Ctrl? + A"` (the question mark indicates that it doesn't matter whether the control modifier is down)
 */
val String.shortcut get() = parseShortcut(this)

private fun checkNotSet(value: ModifierValue, idx: Int, tok: String) {
    if (value != UP) throw ParseException("Duplicate modifier $tok", idx)
}

private fun parseShortcut(s: String): Shortcut {
    var code: KeyCode? = null
    var ctrl = UP
    var shift = UP
    var alt = UP
    val builder = StringBuilder()
    fun tokenComplete(idx: Int) {
        when (val tok = builder.toString().toUpperCase()) {
            "CTRL" -> {
                checkNotSet(ctrl, idx, tok)
                ctrl = DOWN
            }
            "CTRL?" -> {
                checkNotSet(ctrl, idx, tok)
                ctrl = MAYBE
            }
            "SHIFT" -> {
                checkNotSet(shift, idx, tok)
                shift = DOWN
            }
            "SHIFT?" -> {
                checkNotSet(shift, idx, tok)
                shift = MAYBE
            }
            "ALT" -> {
                checkNotSet(alt, idx, tok)
                alt = DOWN
            }
            "ALT?" -> {
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
            '+' -> tokenComplete(idx)
            ' ' -> {
            }
            else -> builder.append(c)
        }
    }
    tokenComplete(s.length - 1)
    if (code == null) throw ParseException("Illegal shortcut, has no main key", 0)
    return Shortcut(code, ctrl, alt, shift)
}

/**
 * Register a new event handler for the given key event type
 */
inline fun Node.registerShortcuts(
    eventType: EventType<KeyEvent> = KeyEvent.KEY_RELEASED,
    crossinline handle: KeyEventHandlerBody<Unit>.() -> Unit
) {
    addEventHandler(eventType) { ev: KeyEvent ->
        KeyEventHandlerBody(Unit, ev).handle()
    }
}

/**
 * Register a new event handler for the given key event type
 */
inline fun <R : Any> Node.registerShortcuts(
    receiver: R,
    eventType: EventType<KeyEvent> = KeyEvent.KEY_RELEASED,
    crossinline handle: KeyEventHandlerBody<R>.() -> Unit
) {
    val ref = WeakReference(receiver)
    addEventHandler(eventType) { ev ->
        val referent = ref.get()
        if (referent != null) KeyEventHandlerBody(referent, ev).handle()
    }
}

/**
 * Register a new event filter for the given key event type
 */
inline fun Scene.registerShortcuts(
    eventType: EventType<KeyEvent> = KeyEvent.KEY_RELEASED,
    crossinline handle: KeyEventHandlerBody<Unit>.() -> Unit
) {
    addEventFilter(eventType) { ev: KeyEvent ->
        KeyEventHandlerBody(Unit, ev).handle()
    }
}