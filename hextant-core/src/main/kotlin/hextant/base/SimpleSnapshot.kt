/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.core.Editor
import kserial.CompoundSerializable

/**
 * A snapshot, that does
 */
class SimpleSnapshot<Original : Editor<*>>(private val original: Original) : EditorSnapshot<Original>(original),
                                                                             CompoundSerializable {
    override fun reconstruct(editor: Original) {}

    override fun components(): Sequence<Any> = sequenceOf(original)
}