package hextant.main.view

import hextant.completion.*
import hextant.context.Context
import hextant.main.HextantPlatform.marketplace
import hextant.main.plugins.PluginManager
import hextant.plugins.PluginInfo
import hextant.plugins.PluginProperty
import kotlinx.coroutines.runBlocking

internal class DisabledPluginInfoCompleter(private val types: Set<PluginInfo.Type>) : Completer<Context, PluginInfo> {
    override fun completions(context: Context, input: String): Collection<Completion<PluginInfo>> = runBlocking {
        val excluded = context[PluginManager].enabledIds()
        val completions = context[marketplace].getPlugins(input, 10, types, excluded)
        completions.map { id ->
            val info = context[marketplace].get(PluginProperty.info, id)!!
            val match = CompletionStrategy.simple.match(input, id) as CompletionResult.Match
            val tooltipText = info.description
            Completion(info, input, id, match.matchedRegions, tooltipText, "${info.name} by ${info.author}", null)
        }
    }
}