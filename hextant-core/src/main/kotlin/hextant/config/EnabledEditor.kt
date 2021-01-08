/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import validated.*

internal class EnabledEditor(context: Context, val enabled: Boolean) :
    TokenEditor<Validated<Enabled>, TokenEditorView>(context) {
    override fun compile(item: Any): Validated<Enabled> =
        (item as? Enabled).validated { invalidComponent }
}