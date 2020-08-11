/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import hextant.plugins.AspectType.Regular
import kotlinx.serialization.Serializable

@Serializable
enum class AspectType {
    Regular, Instance, Provider
}

@Serializable
data class Aspect(
    val name: String,
    val target: String,
    val optional: Boolean = false,
    val type: AspectType = Regular
)

@Serializable
data class Case(val name: String, val interfaces: List<String>)

@Serializable
data class Dependency(val id: String)

@Serializable
data class Implementation(
    val name: String,
    val aspect: String,
    val case: String
)

@Serializable
data class ImplementationRequest(val aspect: String, val case: String)

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
    val dependencies: List<Dependency> = emptyList(),
    val projectTypes: List<ProjectType> = emptyList(),
    val aspects: List<Aspect> = emptyList(),
    val cases: List<Case> = emptyList(),
    val implementations: List<Implementation> = emptyList()
) {
    @Serializable
    enum class Type {
        Library, Language, Global, Local
    }
}

@Serializable
data class ImplementationBundle(
    val id: String,
    val author: String,
    val implementations: List<Implementation>,
    val dependencies: List<Dependency>
)

@Serializable
data class PluginSearch(val searchText: String, val limit: Int, val types: Set<Plugin.Type>, val excluded: Set<String>)