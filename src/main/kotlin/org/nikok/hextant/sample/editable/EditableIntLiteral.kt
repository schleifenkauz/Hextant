/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editable

import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.sample.ast.IntLiteral

class EditableIntLiteral : EditableToken<IntLiteral>() {
    override fun isValid(tok: String): Boolean = tok.toIntOrNull() != null

    override fun compile(tok: String): IntLiteral = IntLiteral(tok.toInt())
}