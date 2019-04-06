/**
 *@author Nikolaus Knop
 */

package hackus.editable

import hackus.ast.RightSide.Compound
import hextant.*
import hextant.base.AbstractEditable

class EditableCompound: AbstractEditable<Compound>(), EditableRightSide<Compound> {
    val subNodes = EditableSubNodeList()

    override val result = result1(subNodes) { nodes -> ok(Compound(nodes)) }
}