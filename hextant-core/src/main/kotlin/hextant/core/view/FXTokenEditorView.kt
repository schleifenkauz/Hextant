/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.Context
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.core.editor.TokenEditor

/**
 * JavaFX implementation of a [TokenEditorView]
 */
open class FXTokenEditorView(
    editor: TokenEditor<*, TokenEditorView>,
    args: Bundle,
    completer: Completer<Context, String> = NoCompleter
) : AbstractTokenEditorControl(editor, args, completer) {
    init {
        @Suppress("LeakingThis")
        editor.addView(this)
    }
}