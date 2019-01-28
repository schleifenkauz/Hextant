/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import kserial.Serializable

class EditableText : EditableToken<String>(), Serializable {
    override fun isValid(tok: String): Boolean = true

    override fun compile(tok: String): String = tok
}