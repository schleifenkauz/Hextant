/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.core.base.EditableToken
import org.nikok.hextant.sample.ast.Name

class EditableName : EditableToken<Name>() {
    override fun isValid(tok: String): Boolean = tok.all { it.isLetter() }

    override fun compile(tok: String): Name = Name(tok)
}