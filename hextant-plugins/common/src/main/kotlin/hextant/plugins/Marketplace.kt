/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import java.io.File

interface Marketplace {
    suspend fun getPlugins(
        searchText: String,
        limit: Int,
        types: Set<PluginInfo.Type>,
        excluded: Set<String>
    ): List<String>

    suspend fun getImplementation(aspect: String, feature: String): ImplementationCoord?

    suspend fun availableProjectTypes(): List<LocatedProjectType>

    suspend fun getProjectType(name: String): LocatedProjectType?

    suspend fun getJarFile(id: String): File?

    suspend fun <T : Any> get(property: PluginProperty<T>, pluginId: String): T?

    suspend fun upload(jar: File)
}

