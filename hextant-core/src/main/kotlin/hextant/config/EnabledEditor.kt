/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.Context
import hextant.core.editor.TokenEditor
import validated.*

internal class EnabledEditor(context: Context) : TokenEditor<Enabled>(context) {
    override fun compile(item: Any): Validated<Enabled> = (item as? Enabled).validated { invalidComponent }

    override fun compile(token: String): Validated<Enabled> = invalidComponent
}