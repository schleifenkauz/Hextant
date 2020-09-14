package hextant.simple

import hextant.codegen.ProvideProjectType
import hextant.context.Context
import hextant.core.Editor
import hextant.project.ProjectType
import hextant.simple.editor.ProgramEditor


@ProvideProjectType("Sample Project")
object SampleProject : ProjectType {
    override fun createProject(context: Context): Editor<*> = ProgramEditor(context)
}