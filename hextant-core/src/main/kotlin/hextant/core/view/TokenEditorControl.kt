/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.core.editor.TokenEditor

/**
 * Default implementation of [AbstractTokenEditorControl]
 */
open class TokenEditorControl(editor: TokenEditor<*, TokenEditorView>, args: Bundle) :
    AbstractTokenEditorControl(editor, args) {
    init {
        editor.addView(this)
    }
}