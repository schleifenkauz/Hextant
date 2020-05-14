/**
 *@author Nikolaus Knop
 */

package hextant.serial

import bundles.Property
import hextant.core.Internal

class IdGenerator {
    private var state = 0

    fun generateID(): String {
        val id = state
        state++
        return "$id"
    }

    companion object : Property<IdGenerator, Internal, Internal>("path generator", default = IdGenerator())
}