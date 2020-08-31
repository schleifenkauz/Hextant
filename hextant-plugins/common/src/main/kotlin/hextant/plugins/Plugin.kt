package hextant.plugins

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class Plugin(val id: String, private val marketplace: Marketplace, scope: CoroutineScope) {
    override fun equals(other: Any?): Boolean = other is Plugin && other.id == this.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id

    val info = scope.async { marketplace.get(PluginProperty.info, id)!! }
    val aspects = scope.async { marketplace.get(PluginProperty.aspects, id).orEmpty() }
    val features = scope.async { marketplace.get(PluginProperty.features, id).orEmpty() }
    val implementations = scope.async { marketplace.get(PluginProperty.implementations, id).orEmpty() }
}