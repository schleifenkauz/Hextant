package hextant.lisp.editor

import bundles.set
import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.CompoundEditor
import hextant.lisp.SExpr
import hextant.lisp.ctx.EditingContext
import hextant.project.ProjectType
import reaktive.value.ReactiveValue

class LispProject(context: Context) : CompoundEditor<SExpr?>(context) {
    val root by child(SExprExpander(context,))

    override val result: ReactiveValue<SExpr?> get() = root.result

    @ProvideProjectType(name = "Lisp Project")
    companion object : ProjectType {
        override fun initializeContext(context: Context) {
            context[EditingContext] = EditingContext.File
        }

        override fun createProject(context: Context): Editor<*> = LispProject(context)
    }
}