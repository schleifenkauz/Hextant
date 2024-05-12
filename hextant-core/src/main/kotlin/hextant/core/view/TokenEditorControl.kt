/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.set
import hextant.codegen.ProvideImplementation
import hextant.completion.Completer
import hextant.completion.NoCompleter
import hextant.context.ControlFactory
import hextant.core.editor.TokenEditor

/**
 * Default implementation of [AbstractTokenEditorControl]
 */
open class TokenEditorControl @ProvideImplementation(ControlFactory::class) constructor(
    editor: TokenEditor<*, TokenEditorView>,
    args: Bundle
) : AbstractTokenEditorControl(editor, args) {
    constructor(
        editor: TokenEditor<*, TokenEditorView>,
        args: Bundle,
        completer: Completer<TokenEditor<*, *>, Any> = NoCompleter,
        styleClass: String? = null
    ) : this(editor, args.also { it[COMPLETER] = completer }) {
        if (styleClass != null) root.styleClass.add(styleClass)
    }

    init {
        editor.addView(this)
    }
}