/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView

internal class EnabledEditor(context: Context, val enabled: Boolean) :
    TokenEditor<Enabled, TokenEditorView>(context) {
    override fun wrap(item: Any): Enabled? = item as? Enabled
}