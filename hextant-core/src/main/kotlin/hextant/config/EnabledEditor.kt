/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import validated.*

internal class EnabledEditor(context: Context) : TokenEditor<Enabled, TokenEditorView>(context) {
    override fun compile(item: Any): Validated<Enabled> = (item as? Enabled).validated { invalidComponent }

    override fun compile(token: String): Validated<Enabled> = invalidComponent
}