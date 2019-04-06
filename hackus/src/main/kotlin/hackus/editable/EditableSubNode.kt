/**
 *@author Nikolaus Knop
 */

package hackus.editable

import hackus.ast.SubNode
import hextant.Ok
import hextant.base.AbstractEditable
import hextant.result2

class EditableSubNode : AbstractEditable<SubNode>() {
    val name = EditableJIdent()

    val type = EditableFQName()

    override val result = result2(name, type) { n, t -> Ok(SubNode(n, t)) }
}