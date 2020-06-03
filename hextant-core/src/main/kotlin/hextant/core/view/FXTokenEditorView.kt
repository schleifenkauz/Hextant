/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.core.editor.TokenEditor

/**
 * JavaFX implementation of a [TokenEditorView]
 */
open class FXTokenEditorView(
    editor: TokenEditor<*, TokenEditorView>,
    args: Bundle
) : AbstractTokenEditorControl(editor, args) {
    init {
        @Suppress("LeakingThis")
        editor.addView(this)
    }
}