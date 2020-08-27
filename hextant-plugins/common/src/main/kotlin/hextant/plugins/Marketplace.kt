/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import java.io.File

interface Marketplace {
    fun getPlugins(searchText: String, limit: Int, types: Set<PluginInfo.Type>, excluded: Set<String>): List<String>

    fun getImplementation(aspect: String, feature: String): ImplementationCoord?

    fun availableProjectTypes(): List<LocatedProjectType>

    fun getJarFile(id: String): File?

    fun <T : Any> get(property: PluginProperty<T>, pluginId: String): T?

    fun upload(jar: File)
}

