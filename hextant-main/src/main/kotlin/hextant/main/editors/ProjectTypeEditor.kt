/**
 *@author Nikolaus Knop
 */

package hextant.main.editors

import hextant.context.Context
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.plugins.LocatedProjectType
import validated.*

class ProjectTypeEditor(context: Context) : TokenEditor<LocatedProjectType, TokenEditorView>(context) {
    override fun compile(item: Any): Validated<LocatedProjectType> = when (item) {
        is LocatedProjectType -> valid(item)
        else                  -> invalidComponent
    }
}