/**
 * @author Nikolaus Knop
 */

package hextant.fx

import javafx.scene.*
import javafx.scene.input.*

private enum class Mode { Input, Command }

private val EXIT_INPUT_MODE = KeyCodeCombination(KeyCode.ESCAPE)
private val INPUT_MODE = KeyCodeCombination(KeyCode.I)

internal fun Scene.configureVim() {
    var mode = Mode.Input
    addEventFilter(KeyEvent.KEY_RELEASED) { ev ->
        var consume = true
        when {
            EXIT_INPUT_MODE.match(ev) && mode == Mode.Input -> {
                mode = Mode.Command
                root.forEachHextantTextField { tf -> tf.isEditable = false }
            }
            INPUT_MODE.match(ev) && mode == Mode.Command    -> {
                mode = Mode.Input
                root.forEachHextantTextField { tf -> tf.isEditable = true }
            }
            else                                            -> consume = false
        }
        if (consume) ev.consume()
    }
}

private fun Node.forEachHextantTextField(action: (HextantTextField) -> Unit) {
    when (this) {
        is HextantTextField -> action(this)
        is Parent           -> childrenUnmodifiable.forEach { it.forEachHextantTextField(action) }
    }
}
