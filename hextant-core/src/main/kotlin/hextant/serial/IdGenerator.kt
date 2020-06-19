/**
 *@author Nikolaus Knop
 */

package hextant.serial

import bundles.Property
import hextant.context.Internal

/**
 * Generates unique file names.
 */
class IdGenerator {
    private var state = 0

    /**
     * Generates a new unique id that can be used as a file name.
     */
    fun generateID(): String {
        val id = state
        state++
        return "$id"
    }

    companion object : Property<IdGenerator, Internal, Internal>("path generator") {
        override val default: IdGenerator = IdGenerator()
    }
}