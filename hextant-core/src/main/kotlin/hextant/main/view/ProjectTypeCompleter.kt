package hextant.main.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.main.HextantApp
import hextant.plugins.LocatedProjectType

object ProjectTypeCompleter : ConfiguredCompleter<Context, LocatedProjectType>(CompletionStrategy.simple) {
    override fun completionPool(context: Context): Collection<LocatedProjectType> =
        context[HextantApp.marketplace].availableProjectTypes()

    override fun extractText(context: Context, item: LocatedProjectType): String? = item.name

    override fun Builder<LocatedProjectType>.configure(context: Context) {
        infoText = completion.pluginId
    }
}