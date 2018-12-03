/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.sample.ast.Name

/**
 * An editable identifier which is valid if the text only contains letters
 */
class EditableName : EditableToken<Name>() {
    override fun isValid(tok: String): Boolean = tok.all { it.isLetter() }

    override fun compile(tok: String): Name = Name(tok)
}