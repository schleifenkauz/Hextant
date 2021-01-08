/**
 *@author Nikolaus Knop
 */

package hextant.launcher

import bundles.PublicProperty
import bundles.property
import java.io.File

internal class Files(val root: File) {
    init {
        get("projects").mkdirs()
    }

    operator fun get(name: String): File = root.resolve(name)

    fun getProject(name: String): File = get(PROJECTS).resolve(name)

    companion object : PublicProperty<Files> by property("global directory") {
        fun inUserHome(): Files {
            val home = File(System.getProperty("user.home"))
            return Files(home.resolve("hextant"))
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