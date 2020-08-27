package hextant.plugins

class Plugin(val id: String, val marketplace: Marketplace) {
    override fun equals(other: Any?): Boolean = other is Plugin && other.id == this.id

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = id

    val info by background { marketplace.get(PluginProperty.info, id)!! }
    val aspects by background { marketplace.get(PluginProperty.aspects, id).orEmpty() }
    val features by background { marketplace.get(PluginProperty.features, id).orEmpty() }
    val implementations by background { marketplace.get(PluginProperty.implementations, id).orEmpty() }
}