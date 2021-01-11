package hextant.lisp.editor

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.CompoundEditor
import hextant.lisp.SExpr
import hextant.project.ProjectType
import reaktive.value.ReactiveValue

class LispProject(context: Context) : CompoundEditor<SExpr?>(context) {
    val root by child(SExprExpander(context))

    override val result: ReactiveValue<SExpr?> get() = root.result

    @ProvideProjectType(name = "Lisp Project")
    companion object : ProjectType {
        override fun createProject(context: Context): Editor<*> = LispProject(context)
    }
}