/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.view.TokenEditorView
import validated.Validated
import validated.valid

/**
 * A [TokenEditor] that accepts every string.
 */
class SimpleStringEditor(context: Context) : TokenEditor<String, TokenEditorView>(context) {
    override fun compile(token: String): Validated<String> = valid(token)
}