package hextant.cli

import java.io.File

object HextantDirectory {
    private val root = run {
        val specified = System.getenv("HEXTANT_HOME")
        val default = System.getProperty("user.home") + "/hextant"
        File(specified ?: default)
    }

    private val openedProjects = mutableSetOf<File>()

    init {
        get("projects").mkdirs()
    }

    operator fun get(name: String): File = root.resolve(name)

    fun resolve(vararg path: String) = root.resolve(path.joinToString("/"))

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

    const val PROJECTS = "projects"
    const val PLUGIN_CACHE = "plugins"
    const val PROJECT_INFO = "project.json"
    const val PROJECT_ROOT = "root.json"
    const val DISPLAY = "display.json"
    const val GLOBAL_PLUGINS = "global-plugins.json"
    const val LOCK = ".lock"
}