/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.view.TokenEditorView

/**
 * A [TokenEditor] that accepts every string.
 */
class SimpleStringEditor(context: Context) : TokenEditor<String, TokenEditorView>(context) {
    override fun wrap(token: String): String = token
}