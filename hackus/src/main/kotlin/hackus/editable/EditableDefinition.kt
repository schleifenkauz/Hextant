/**
 *@author Nikolaus Knop
 */

package hackus.editable

import hackus.ast.Definition
import hextant.Ok
import hextant.base.AbstractEditable
import hextant.result2

class EditableDefinition : AbstractEditable<Definition>() {
    val name = EditableFQName()

    val rightSide = ExpandableRightSide()

    override val result = result2(name, rightSide) { n, r -> Ok(Definition(n, r)) }
}