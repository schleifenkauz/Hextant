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
data class LocatedProjectType(val name: String, val clazz: String, val pluginId: String) {
    override fun toString(): String = name
}

@Serializable
data class PluginInfo(
    val id: String,
    val name: String,
    val author: String,
    val type: Type,
    val description: String? = null,
    val initializer: String? = null,
    val dependencies: List<Dependency> = emptyList()
) {
    override fun toString(): String = id

    @Serializable
    enum class Type {
        Language, Global, Local;

        companion object {
            val all = values().toSet()
        }
    }
}

@Serializable
data class PluginSearch(
    val searchText: String,
    val limit: Int,
    val types: Set<PluginInfo.Type>,
    val excluded: Set<String>
)

@Serializable
data class ProjectInfo(
    val projectType: ProjectType,
    val enabledPlugins: List<String>,
    val requiredPlugins: List<String>
)