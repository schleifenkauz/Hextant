/**
 * @author Nikolaus Knop
 */

package hextant.fx

import hextant.main.InputMethod
import javafx.scene.Node
import javafx.scene.Parent

private fun Node.forEachHextantTextField(action: (HextantTextField) -> Unit) {
    when (this) {
        is HextantTextField -> action(this)
        is Parent           -> childrenUnmodifiable.forEach { it.forEachHextantTextField(action) }
    }
}

/**
 * Apply the given input-[mode] to all [HextantTextField]'s which are children of this node.
 */
fun Node.applyInputMethod(mode: InputMethod) {
    forEachHextantTextField {
        it.inputMethod = mode
    }
}