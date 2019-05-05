/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.sample.ast.Name

/**
 * An editable identifier which is valid if the text only contains letters
 */
class NameEditor(context: Context) : TokenEditor<Name>(context) {
    override fun compile(token: String): CompileResult<Name> =
        token.takeIf { token.all { it.isLetter() } }.okOrErr { "Invalid name $token" }.map { Name(it) }
}