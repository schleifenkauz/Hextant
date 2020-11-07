/**
 *@author Nikolaus Knop
 */

package hextant.serial

import bundles.PublicProperty
import bundles.property

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

    internal companion object : PublicProperty<IdGenerator> by property("path generator", default = IdGenerator())
}