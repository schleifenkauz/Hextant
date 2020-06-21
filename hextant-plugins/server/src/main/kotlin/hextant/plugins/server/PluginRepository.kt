/**
 * @author Nikolaus Knop
 */

package hextant.plugins.server

import hextant.plugins.Plugin
import java.io.File

interface PluginRepository {
    fun getAllPlugins(): List<String>

    fun getPluginById(id: String): Plugin?

    fun getJarFile(pluginId: String): File?
}