package hextant.config

interface Feature {
    val id: String

    val type: FeatureType<*>

    val description: String

    val enabledByDefault: Boolean
}