/**
 *@author Nikolaus Knop
 */

package hextant.plugins

import kotlinx.serialization.Serializable

@Serializable
data class Plugin(
    val id: String,
    val name: String,
    val author: String,
    val type: Type,
    val description: String,
    val initializerClass: String,
    val dependencies: List<String> = emptyList(),
    val projectTypes: List<String> = emptyList()
) {
    enum class Type {
        Language, Global, Local
    }
}