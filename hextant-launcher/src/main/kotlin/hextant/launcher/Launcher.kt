package hextant.launcher

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.Editor
import hextant.core.EditorView
import hextant.core.editor.AbstractEditor
import hextant.project.ProjectType
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue

class Launcher(context: Context) : AbstractEditor<Unit, EditorView>(context) {
    override val result: ReactiveValue<Unit> = reactiveValue(Unit)

    @ProvideProjectType("Launcher")
    companion object: ProjectType {
        override fun createProject(context: Context): Editor<*> = Launcher(context)
    }
}