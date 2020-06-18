/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.sample.ast.Name
import validated.*

/**
 * An editable identifier which is valid if the text only contains letters
 */
class NameEditor(context: Context) : TokenEditor<Name, TokenEditorView>(context) {
    override fun compile(token: String): Validated<Name> =
        token.takeIf { token.all { it.isLetter() } }.validated { invalid("Invalid name $token") }.map { Name(it) }
}