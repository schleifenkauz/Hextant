/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.expr.editable

import kserial.Serializable
import org.nikok.hextant.core.editable.EditableToken

class EditableText : EditableToken<String>(), Serializable {
    override fun isValid(tok: String): Boolean = true

    override fun compile(tok: String): String = tok
}