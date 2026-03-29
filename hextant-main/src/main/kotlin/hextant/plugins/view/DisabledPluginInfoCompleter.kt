package hextant.plugins.view

import hextant.completion.Completer
import hextant.completion.Completion
import hextant.completion.CompletionCollector
import hextant.completion.CompletionResult.Match
import hextant.completion.CompletionStrategy
import hextant.context.Properties.marketplace
import hextant.core.Editor
import hextant.plugins.PluginInfo
import hextant.plugins.PluginManager
import hextant.plugins.PluginProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class DisabledPluginInfoCompleter(private val types: Set<PluginInfo.Type>) : Completer<Editor<*>> {
    override suspend fun CoroutineScope.collectCompletions(
        context: Editor<*>, input: String,
        collector: CompletionCollector
    ) {
        val ctx = context.context
        val excluded = ctx[PluginManager].enabledPlugins().mapTo(mutableSetOf()) { it.id }
        val completions = ctx[marketplace].getPlugins(input, 10, types, excluded)
        val jobs = mutableListOf<Deferred<Unit>>()
        for (id in completions) {
            val job = async {
                val info = ctx[marketplace].get(PluginProperty.info, id)!!
                val match = CompletionStrategy.simple.match(input, info.id)
                if (match is Match) {
                    val tooltipText = info.description
                    collector.offerCompletion(match.similarity) {
                        Completion(
                            item = info,
                            inputText = input,
                            completionText = info.id,
                            match = match.matchedRegions,
                            tooltipText = tooltipText,
                            infoText = "${info.name} by ${info.author}",
                            icon = null, similarity = match.similarity, source = this@DisabledPluginInfoCompleter
                        )
                    }
                }
            }
            jobs.add(job)
        }
        jobs.awaitAll()
        collector.finished()
    }
}