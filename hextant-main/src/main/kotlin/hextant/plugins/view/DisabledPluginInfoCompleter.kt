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
import kotlinx.coroutines.*

internal class DisabledPluginInfoCompleter(private val types: Set<PluginInfo.Type>) : Completer<Editor<*>> {
    override suspend fun collectCompletions(
        context: Editor<*>, input: String,
        collector: CompletionCollector
    ) = withContext(Dispatchers.Default) {
        val ctx = context.context
        val excluded = ctx[PluginManager].enabledPlugins().mapTo(mutableSetOf()) { it.id }
        val completions = ctx[marketplace].getPlugins(input, 10, types, excluded)
        val jobs = mutableListOf<Job>()
        for (id in completions) {
            val job = launch {
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
        jobs.joinAll()
        collector.finished()
    }
}