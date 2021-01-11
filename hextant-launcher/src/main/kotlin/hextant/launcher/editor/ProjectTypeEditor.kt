/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.codegen.ProvideImplementation
import hextant.context.Context
import hextant.context.EditorFactory
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.launcher.HextantPlatform.marketplace
import hextant.plugins.LocatedProjectType
import kotlinx.coroutines.runBlocking

internal class ProjectTypeEditor @ProvideImplementation(EditorFactory::class) constructor(
    context: Context
) : TokenEditor<LocatedProjectType?, TokenEditorView>(context) {
    override fun compile(token: String): LocatedProjectType? {
        if (token.isBlank()) return null
        return runBlocking { context[marketplace].getProjectType(token) }
    }
}