package hextant.expr.editor

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.Editor
import hextant.project.ProjectType

@ProvideProjectType("Expression")
object ExpressionProject : ProjectType {
    override fun createProject(context: Context): Editor<*> = ExprExpander(context)
}