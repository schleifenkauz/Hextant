package hextant.main.view

import hextant.completion.*
import hextant.completion.CompletionResult.Match
import hextant.context.Context
import hextant.main.HextantPlatform.marketplace
import hextant.main.plugins.PluginManager
import hextant.plugins.PluginInfo
import hextant.plugins.PluginProperty
import kotlinx.coroutines.*

internal class DisabledPluginInfoCompleter(private val types: Set<PluginInfo.Type>) : Completer<Context, PluginInfo> {
    override fun completions(context: Context, input: String): Collection<Completion<PluginInfo>> = runBlocking {
        val excluded = context[PluginManager].enabledIds()
        val completions = context[marketplace].getPlugins(input, 10, types, excluded)
        completions
            .map { id -> async { context[marketplace].get(PluginProperty.info, id)!! } }.awaitAll()
            .map { info ->
                val match = CompletionStrategy.simple.match(input, info.id) as Match
                val tooltipText = info.description
                Completion(
                    completion = info,
                    inputText = input,
                    completionText = info.id,
                    match = match.matchedRegions,
                    tooltipText = tooltipText,
                    infoText = "${info.name} by ${info.author}",
                    icon = null
                )
            }
    }
}