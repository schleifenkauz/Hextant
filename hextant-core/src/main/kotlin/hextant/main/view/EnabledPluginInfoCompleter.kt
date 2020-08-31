/**
 * @author Nikolaus Knop
 */

package hextant.main.view

import hextant.completion.Completion.Builder
import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.context.Context
import hextant.main.plugins.PluginManager
import hextant.plugins.PluginInfo

internal object EnabledPluginInfoCompleter : ConfiguredCompleter<Context, PluginInfo>(CompletionStrategy.simple) {
    override fun extractText(context: Context, item: PluginInfo): String? = item.id

    override fun Builder<PluginInfo>.configure(context: Context) {
        infoText = "${completion.name} by ${completion.author}"
        tooltipText = completion.description
    }

    override fun completionPool(context: Context): Collection<PluginInfo> =
        context[PluginManager].enabledPlugins().map { it.info }
}