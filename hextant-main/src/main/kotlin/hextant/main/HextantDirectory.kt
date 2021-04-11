/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.PublicProperty
import bundles.property
import java.io.File

class HextantDirectory(val root: File) {
    private val openedProjects = mutableSetOf<File>()

    init {
        get("projects").mkdirs()
    }

    operator fun get(name: String): File = root.resolve(name)

    fun getProject(name: String): File = get(PROJECTS).resolve(name)

    fun acquireLock(project: File): Boolean {
        val lock = project.resolve(LOCK)
        val possible = lock.createNewFile()
        if (possible) openedProjects.add(project)
        lock.deleteOnExit()
        return possible
    }

    fun releaseLock(project: File) {
        val lock = project.resolve(LOCK)
        openedProjects.remove(project)
        lock.delete()
    }

    fun isLocked(project: File) = project.resolve(LOCK).exists()

    companion object : PublicProperty<HextantDirectory> by property("global directory") {
        fun get(): HextantDirectory {
            val specified = System.getProperty("hextant.home")
            val default = System.getProperty("user.home") + "/hextant"
            return HextantDirectory(File(specified ?: default))
        }

        const val PROJECTS = "projects"
        const val PLUGIN_CACHE = "plugins"
        const val PROJECT_INFO = "project.json"
        const val PROJECT_ROOT = "root.json"
        const val DISPLAY = "display.json"
        const val GLOBAL_PLUGINS = "global-plugins.json"
        const val LOCK = ".lock"
    }
}