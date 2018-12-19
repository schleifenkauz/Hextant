/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.editor

import org.nikok.hextant.Context
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.base.DefaultRegistration
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.command.register
import org.nikok.hextant.core.editor.TokenEditor
import org.nikok.hextant.core.expr.view.TextEditorView
import org.nikok.hextant.sample.editable.EditableName
import org.nikok.reaktive.value.now

class NameEditor(
    editable: EditableName,
    context: Context
) : TokenEditor<EditableName, TextEditorView>(editable, context) {
    init {
        registerDefault(context)
    }

    companion object : DefaultRegistration({
        with(get(Public, Commands).of<NameEditor>()) {
            register<NameEditor, Unit> {
                name = "Toggle case"
                description = "Toggles the case for the selected name, non letter chars are not affected"
                shortName = "tglcs"
                executing { editor, _ ->
                    val before = editor.editable.text.now
                    editor.setText(NameEditor.toggleCase(before))
                }
            }
        }
    }) {
        private fun toggleCase(s: String): String = buildString {
            for (c in s) {
                when {
                    c.isUpperCase() -> this.append(c.toLowerCase())
                    c.isLowerCase() -> this.append(c.toUpperCase())
                    else            -> this.append(c)
                }
            }
        }
    }
}
