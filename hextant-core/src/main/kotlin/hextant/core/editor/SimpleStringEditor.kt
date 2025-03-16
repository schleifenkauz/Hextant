/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.core.view.TokenEditorView
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * A [TokenEditor] that accepts every string.
 */
@Serializable
class SimpleStringEditor : TokenEditor<@Contextual String, TokenEditorView>() {
    override fun compile(token: String): String = token
}