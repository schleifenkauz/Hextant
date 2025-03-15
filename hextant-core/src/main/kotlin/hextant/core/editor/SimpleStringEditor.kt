/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.view.TokenEditorView

/**
 * A [TokenEditor] that accepts every string.
 */
class SimpleStringEditor : TokenEditor<String, TokenEditorView>() {
    override fun compile(token: String): String = token
}