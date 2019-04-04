/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import hextant.CompileResult
import hextant.Ok
import kserial.Serializable

class EditableText : EditableToken<String>(), Serializable {
    override fun compile(tok: String): CompileResult<String> = Ok(tok)
}