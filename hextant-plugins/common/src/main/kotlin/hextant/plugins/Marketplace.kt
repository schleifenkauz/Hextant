/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import java.io.File

interface Marketplace {
    suspend fun getPlugins(searchText: String, limit: Int, types: Set<Plugin.Type>, excluded: Set<String>): List<Plugin>

    suspend fun getPluginById(id: String): Plugin?

    suspend fun getImplementation(aspect: String, case: String): ImplementationBundle?

    suspend fun download(id: String): File?
}