/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.*
import hextant.core.editable.EditableToken
import hextant.sample.ast.Name

/**
 * An editable identifier which is valid if the text only contains letters
 */
class EditableName : EditableToken<Name>() {
    override fun compile(tok: String): CompileResult<Name> =
        tok.takeIf { tok.all { it.isLetter() } }.okOrErr { "Invalid name $tok" }.map { Name(it) }
}