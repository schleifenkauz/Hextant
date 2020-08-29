/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.SimpleProperty
import java.io.File

internal class GlobalDirectory(private val root: File) {
    operator fun get(name: String): File = root.resolve(name)

    companion object : SimpleProperty<GlobalDirectory>("global directory") {
        fun inUserHome(): GlobalDirectory {
            val home = File(System.getProperty("user.home"))
            return GlobalDirectory(home.resolve("hextant"))
        }

        const val PROJECTS = "projects"
        const val PLUGIN_CACHE = "plugin-cache"
        const val PROJECT_INFO = "project.json"
    }
}