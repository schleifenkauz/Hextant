/**
 *@author Nikolaus Knop
 */

package hextant.project.editor

import hextant.*
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView

class FileNameEditor(context: Context, text: String) : TokenEditor<String, TokenEditorView>(context, text) {
    constructor(context: Context) : this(context, "")

    override fun compile(token: String): CompileResult<String> =
        token.takeUnless { it.isBlank() }.okOrErr { "Illegal file name: '$token'" }
}