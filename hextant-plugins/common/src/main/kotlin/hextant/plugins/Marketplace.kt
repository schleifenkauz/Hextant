/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import java.io.File

interface Marketplace {
    fun getPlugins(searchText: String, limit: Int, types: Set<Plugin.Type>, excluded: Set<String>): List<Plugin>

    fun getImplementation(aspect: String, feature: String): ImplementationCoord?

    fun getJarFile(id: String): File?
}

