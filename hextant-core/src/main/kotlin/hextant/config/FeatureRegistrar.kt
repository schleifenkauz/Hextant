@file:Suppress("UNCHECKED_CAST")

package hextant.config

import bundles.PublicProperty
import bundles.publicProperty
import hextant.context.Context
import hextant.context.Properties.logger
import kollektion.MultiMap

internal class FeatureRegistrar(private val context: Context) {
    private val byId = mutableMapOf<String, Feature>()
    private val enabled = MultiMap<FeatureType<*>, Feature>()
    private val disabled = MultiMap<FeatureType<*>, Feature>()

    private fun removeFrom(map: MultiMap<FeatureType<*>, Feature>, feature: Feature) {
        for (type in feature.type.chain()) {
            !map[type].remove(feature)
        }
    }

    private fun addTo(map: MultiMap<FeatureType<*>, Feature>, feature: Feature) {
        for (type in feature.type.chain()) {
            map.put(type, feature)
        }
    }

    fun isEnabled(feature: Feature): Boolean = feature in enabled[feature.type]

    fun getFeature(id: String): Feature? = byId[id]

    fun register(feature: Feature) {
        if (feature.id in byId) {
            context[logger].warning("Attempt to register feature $feature with duplicate id")
            return
        }
        if (feature.enabledByDefault) {
            addTo(enabled, feature)
            onEnable(feature)
        } else {
            addTo(disabled, feature)
        }
    }

    fun unregister(feature: Feature) {
        if (feature.id !in byId) {
            context[logger].warning("Feature $feature is already unregistered or was never registered")
        }
        if (isEnabled(feature)) {
            removeFrom(enabled, feature)
            onDisable(feature)
        } else {
            removeFrom(disabled, feature)
        }
    }

    fun <F : Feature> enable(feature: F) {
        if (isEnabled(feature)) {
            context[logger].warning("Feature ${feature.id} is already enabled")
            return
        }
        addTo(enabled, feature)
        onEnable(feature)
    }

    fun <F : Feature> disable(feature: F) {
        if (!isEnabled(feature)) {
            context[logger].warning("Feature ${feature.id} is already disabled")
            return
        }
        removeFrom(enabled, feature)
        addTo(disabled, feature)
        onDisable(feature)
    }

    private fun <F : Feature> onEnable(feature: F) {
        val t = feature.type as FeatureType<F>
        t.onEnable(feature, context)
    }

    private fun <F : Feature> onDisable(feature: F) {
        val t = feature.type as FeatureType<F>
        t.onDisable(feature, context)
    }

    fun <F : Feature> enabledFeatures(type: FeatureType<out F>): Collection<F> = enabled[type] as Collection<F>

    fun <F : Feature> disabledFeatures(type: FeatureType<out F>): Collection<F> = disabled[type] as Collection<F>

    companion object : PublicProperty<FeatureRegistrar> by publicProperty("feature registrar")
}