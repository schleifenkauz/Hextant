/**
 *@author Nikolaus Knop
 */

package hackus.editable

import hackus.ast.RightSide.Terminal
import hextant.*
import hextant.base.AbstractEditable
import reaktive.value.binding.map

class EditableTerminal: AbstractEditable<Terminal>(), EditableRightSide<Terminal> {
    val fqName = EditableFQName()

    override val result = result1(fqName) { n -> ok(Terminal(n)) }
}