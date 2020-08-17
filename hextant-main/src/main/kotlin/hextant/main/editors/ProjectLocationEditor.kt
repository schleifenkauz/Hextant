/**
 *@author Nikolaus Knop
 */

package hextant.main.editors

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import validated.*
import java.io.File
import java.io.IOException

class ProjectLocationEditor(context: Context) : TokenEditor<File, TokenEditorView>(context) {
    override fun compile(token: String): Validated<File> {
        val f = File(token)
        return when {
            !isValidFilePath(f)  -> invalid("Invalid path $token")
            !f.exists()          -> invalid("File $f does not exist")
            f.extension != "hxt" -> invalid("Path $f does not point to a Hextant project")
            else                 -> valid(f)
        }
    }

    override fun compile(item: Any): Validated<File> = when (item) {
        is File -> valid(item)
        else    -> invalidComponent
    }

    private fun isValidFilePath(file: File) = try {
        file.canonicalPath
        true
    } catch (ex: IOException) {
        false
    }
}