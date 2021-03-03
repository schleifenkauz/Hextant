package hextant.config

import hextant.context.Context
import hextant.context.Properties.logger

abstract class FeatureType<F : Feature> private constructor(
    private val parent: FeatureType<in F>?,
    val name: String
) {
    constructor(name: String, parent: FeatureType<in F> = ALL) : this(parent, name)

    override fun toString(): String = name

    open fun onEnable(feature: F, context: Context) {
        parent?.onEnable(feature, context)
    }

    open fun onDisable(feature: F, context: Context) {
        parent?.onDisable(feature, context)
    }

    fun chain(): Sequence<FeatureType<in F>> = generateSequence<FeatureType<in F>>(this) { it.parent }

    object ALL : FeatureType<Feature>(null, "ALL") {
        override fun onEnable(feature: Feature, context: Context) {
            context[logger].config { "Enabled feature ${feature.id}" }
        }

        override fun onDisable(feature: Feature, context: Context) {
            context[logger].config { "Disabled feature ${feature.id}" }
        }
    }
}