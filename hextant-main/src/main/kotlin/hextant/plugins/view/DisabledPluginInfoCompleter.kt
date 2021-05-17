package hextant.plugins.view

import hextant.completion.Completer
import hextant.completion.Completion
import hextant.completion.CompletionResult.Match
import hextant.completion.CompletionStrategy
import hextant.context.Properties.marketplace
import hextant.core.Editor
import hextant.plugins.PluginInfo
import hextant.plugins.PluginManager
import hextant.plugins.PluginProperty
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

internal class DisabledPluginInfoCompleter(private val types: Set<PluginInfo.Type>) : Completer<Editor<*>, PluginInfo> {
    override fun completions(context: Editor<*>, input: String): Collection<Completion<PluginInfo>> = runBlocking {
        val ctx = context.context
        val excluded = ctx[PluginManager].enabledPlugins().mapTo(mutableSetOf()) { it.id }
        val completions = ctx[marketplace].getPlugins(input, 10, types, excluded)
        completions
            .map { id -> async { ctx[marketplace].get(PluginProperty.info, id)!! } }.awaitAll()
            .map { info ->
                val match = CompletionStrategy.simple.match(input, info.id) as Match
                val tooltipText = info.description
                Completion(
                    item = info,
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