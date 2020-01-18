/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.bundle.CorePermissions.Internal
import hextant.bundle.Property
import kserial.*

class PathGenerator : Serializable {
    private var state = 0

    override fun deserialize(input: Input, context: SerialContext) {
        state = input.readInt()
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeInt(state)
    }

    fun genFile(): String = "${++state}"

    companion object : Property<PathGenerator, Internal, Internal>("path generator", default = PathGenerator())
}