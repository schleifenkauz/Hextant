package hextant.lisp.editor

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.Editor
import hextant.core.editor.CompoundEditor
import hextant.lisp.SExpr
import hextant.lisp.rt.RuntimeScope
import hextant.project.ProjectType
import validated.reaktive.ReactiveValidated

class LispProject(context: Context) : CompoundEditor<SExpr>(context) {
    val root by child(SExprExpander(context, RuntimeScopeEditor(context, RuntimeScope.root())))

    override val result: ReactiveValidated<SExpr> get() = root.result

    @ProvideProjectType(name = "Lisp Project")
    companion object : ProjectType {
        override fun createProject(context: Context): Editor<*> = LispProject(context)
    }
}