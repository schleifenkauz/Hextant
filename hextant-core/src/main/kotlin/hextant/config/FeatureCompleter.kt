package hextant.config

import hextant.completion.Completion
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.core.Editor

class FeatureCompleter(private val type: FeatureType<*>, private val enabled: Boolean) :
    ConfiguredCompleter<Editor<*>, Feature>(CompletionStrategy.simple) {
    override fun extractText(context: Editor<*>, item: Feature): String = item.id

    override fun Completion.Builder<Feature>.configure(context: Editor<*>) {
        infoText = completion.description
    }

    override fun completionPool(context: Editor<*>): Collection<Feature> {
        val registrar = context.context[FeatureRegistrar]
        return if (enabled) registrar.enabledFeatures(type) else registrar.disabledFeatures(type)
    }
}