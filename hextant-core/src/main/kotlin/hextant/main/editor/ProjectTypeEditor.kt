/**
 *@author Nikolaus Knop
 */

package hextant.main.editor

import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.main.HextantPlatform.marketplace
import hextant.plugins.LocatedProjectType
import validated.*

internal class ProjectTypeEditor @ProvideImplementation(EditorFactory::class) constructor(
    context: Context
) : TokenEditor<LocatedProjectType, TokenEditorView>(context) {
    override fun compile(item: Any): Validated<LocatedProjectType> = when (item) {
        is LocatedProjectType -> valid(item)
        else                  -> invalidComponent
    }

    override fun compile(token: String): Validated<LocatedProjectType> {
        if (token.isBlank()) return invalidComponent()
        val pt = context[marketplace].getProjectType(token)
        return pt.validated { invalid("No project type with name '$token' found") }
    }
}