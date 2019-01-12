/**
 *@author Nikolaus Knop
 */

package hextant.sample.editable

import hextant.core.editable.EditableToken
import hextant.sample.ast.Name

/**
 * An editable identifier which is valid if the text only contains letters
 */
class EditableName : EditableToken<Name>() {
    override fun isValid(tok: String): Boolean = tok.all { it.isLetter() }

    override fun compile(tok: String): Name = Name(tok)
}