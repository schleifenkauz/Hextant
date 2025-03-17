package hextant.launcher

import bundles.PublicProperty
import bundles.property
import bundles.set
import hextant.cli.CLI
import hextant.cli.HextantDirectory
import hextant.codegen.ProvideProjectType
import hextant.core.Editor
import hextant.core.EditorView
import hextant.core.editor.AbstractEditor
import hextant.plugins.LocatedProjectType
import hextant.project.ProjectType
import kotlinx.serialization.json.JsonElement
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue
import java.io.File

class Launcher : AbstractEditor<Unit, EditorView>() {
    override fun doInitialize() {
        context[Launcher] = this
    }

    fun create(type: LocatedProjectType, dest: File): String = CLI {
        try {
            run("hextant", "--create=${type.name}", dest.absolutePath)
            "Successfully created project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Creating project failed"
        }
    }

    fun open(project: File): String = CLI {
        try {
            run("hextant", project.absolutePath)
            "Successfully opened project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Opening project failed"
        }
    }

    fun delete(project: File): String {
        if (HextantDirectory.isLocked(project)) return "Project opened by another editor"
        return try {
            project.deleteRecursively()
            "Successfully deleted project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception while deleting project: ${e.message}"
        }
    }

    fun rename(project: File, newLocation: File): String {
        if (HextantDirectory.isLocked(project)) return "Cannot rename project: Already opened by another editor"
        project.renameTo(newLocation)
        return "Successfully renamed project"
    }

    override val result: ReactiveValue<Unit> = reactiveValue(Unit)

    override fun serialize(): JsonElement {
        TODO("Not yet implemented")
    }

    override fun deserialize(element: JsonElement) {
        TODO("Not yet implemented")
    }

    @ProvideProjectType("Launcher")
    companion object : ProjectType, PublicProperty<Launcher> by property("launcher") {
        override fun createProject(): Editor<*> {
            return Launcher()
        }
    }
}