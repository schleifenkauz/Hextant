/**
 *@author Nikolaus Knop
 */

package hextant.sample.editor

import hextant.Context
import hextant.base.DefaultRegistration
import hextant.bundle.CorePermissions.Public
import hextant.command.Commands
import hextant.command.register
import hextant.core.editor.TokenEditor
import hextant.core.expr.view.TextEditorView
import hextant.sample.editable.EditableName
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
