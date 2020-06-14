/**
 *@author Nikolaus Knop
 */

package hextant.config

import hextant.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import validated.*

internal class EnabledEditor(context: Context) : TokenEditor<Enabled, TokenEditorView>(context) {
    override fun compile(token: String): Validated<Enabled> = context[EnabledSource].all().find { it.name == token }
        .validated { invalid("No object with name '$token' found") }
}