/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import kotlinx.serialization.Serializable

@Serializable
data class Aspect(
    val name: String,
    val target: String,
    val optional: Boolean = false
)

@Serializable
data class Feature(val name: String, val supertypes: List<String>)

@Serializable
data class Dependency(val id: String)

@Serializable
data class Implementation(
    val clazz: String,
    val aspect: String,
    val feature: String
)

@Serializable
data class ImplementationRequest(val aspect: String, val feature: String)

@Serializable
data class ImplementationCoord(val bundle: String?, val clazz: String)

@Serializable
data class ProjectType(val name: String, val clazz: String)

@Serializable
data class Plugin(
    val id: String,
    val name: String,
    val author: String,
    val type: Type,
    val description: String? = null,
    val initializer: String? = null,
    val dependencies: List<Dependency> = emptyList()
) {
    @Serializable
    enum class Type {
        Library, Language, Global, Local
    }
}

@Serializable
data class PluginSearch(val searchText: String, val limit: Int, val types: Set<Plugin.Type>, val excluded: Set<String>)