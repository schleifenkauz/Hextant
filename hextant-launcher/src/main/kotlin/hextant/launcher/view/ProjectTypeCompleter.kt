package hextant.launcher.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Properties.marketplace
import hextant.core.Editor
import hextant.plugins.LocatedProjectType
import hextant.plugins.Marketplace
import kotlinx.coroutines.runBlocking

internal object ProjectTypeCompleter : ConfiguredCompleter<Editor<*>, LocatedProjectType>(CompletionStrategy.simple) {
    private var projectTypes = mutableMapOf<Marketplace, List<LocatedProjectType>>()

    override fun completionPool(context: Editor<*>): Collection<LocatedProjectType> {
        val marketplace = context.context[marketplace]
        return projectTypes.getOrPut(marketplace) {
            runBlocking { marketplace.availableProjectTypes() }
        }
    }

    override fun extractText(context: Editor<*>, item: LocatedProjectType): String = item.name

    override fun Builder<LocatedProjectType>.configure(context: Editor<*>) {
        infoText = completion.pluginId
    }
}