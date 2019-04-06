/**
 *@author Nikolaus Knop
 */

package hackus.editable

import hackus.ast.RightSide.Alternative
import hextant.*
import hextant.base.AbstractEditable
import reaktive.value.binding.map

class EditableAlternative: AbstractEditable<Alternative>(), EditableRightSide<Alternative> {
    val alternatives = EditableFQNameList()

    override val result = result1(alternatives) { alts -> ok(Alternative(alts)) }
}