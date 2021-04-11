package hextant.launcher

import hextant.context.Context
import hextant.install.CLI
import hextant.plugins.LocatedProjectType
import hextant.main.HextantDirectory
import java.io.File

class ProjectManager(private val context: Context) {
    fun create(type: LocatedProjectType, name: String): String = CLI {
        try {
            val path = context[HextantDirectory].getProject(name).absolutePath
            run("hextant", "--create=${type.clazz}", path)
            "Successfully created project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Creating project failed"
        }
    }

    fun open(name: String): String = CLI {
        try {
            val path = context[HextantDirectory].getProject(name).absolutePath
            run("hextant", path)
            "Successfully opened project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Opening project failed"
        }
    }

    fun delete(project: File): String {
        if (context[HextantDirectory].isLocked(project)) return "Project opened by another editor"
        return try {
            project.deleteRecursively()
            "Successfully deleted project"
        } catch (e: Exception) {
            e.printStackTrace()
            "Exception while deleting project: ${e.message}"
        }
    }

    fun renameProject(project: File, newLocation: File): String {
        if (context[HextantDirectory].isLocked(project)) return "Cannot rename project: Already opened by another editor"
        project.renameTo(newLocation)
        return "Successfully renamed project"
    }
}