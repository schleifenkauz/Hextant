package hextant.main.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.main.HextantPlatform.marketplace
import hextant.plugins.LocatedProjectType
import hextant.plugins.Marketplace
import kotlinx.coroutines.runBlocking

internal object ProjectTypeCompleter : ConfiguredCompleter<Context, LocatedProjectType>(CompletionStrategy.simple) {
    private var projectTypes = mutableMapOf<Marketplace, List<LocatedProjectType>>()

    override fun completionPool(context: Context): Collection<LocatedProjectType> {
        val marketplace = context[marketplace]
        return projectTypes.getOrPut(marketplace) {
            runBlocking { marketplace.availableProjectTypes() }
        }
    }

    override fun extractText(context: Context, item: LocatedProjectType): String? = item.name

    override fun Builder<LocatedProjectType>.configure(context: Context) {
        infoText = completion.pluginId
    }
}