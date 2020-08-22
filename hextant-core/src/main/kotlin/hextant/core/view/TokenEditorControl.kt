/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import hextant.completion.Completer
import hextant.context.Context
import hextant.core.editor.TokenEditor

/**
 * Default implementation of [AbstractTokenEditorControl]
 */
open class TokenEditorControl(editor: TokenEditor<*, TokenEditorView>, args: Bundle) :
    AbstractTokenEditorControl(editor, args) {
    constructor(
        editor: TokenEditor<*, TokenEditorView>,
        args: Bundle,
        completer: Completer<Context, Any>? = null,
        styleClass: String? = null
    ) : this(editor, args) {
        if (completer != null) arguments[COMPLETER] = completer
        if (styleClass != null) root.styleClass.add(styleClass)
    }

    init {
        editor.addView(this)
    }
}