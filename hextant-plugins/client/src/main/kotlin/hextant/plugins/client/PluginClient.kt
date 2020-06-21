/**
 * @author Nikolaus Knop
 */

package hextant.plugins.client

import hextant.plugins.Plugin
import java.io.File

interface PluginClient {
    suspend fun getAllPlugins(): List<String>

    suspend fun getById(id: String): Plugin

    suspend fun getJarFile(pluginId: String): File
}