/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.gui

import org.nikok.hextant.core.base.AbstractController
import org.nikok.hextant.core.completion.*
import org.nikok.hextant.core.impl.myLogger
import kotlin.properties.Delegates.observable

class SearchableListController<E : Any>(
    private val source: Collection<E>,
    private val completer: Completer<E> = DEFAULT_COMPLETER,
    private val maxItems: Int = 100,
    initialText: String = ""
) : AbstractController<SearchableListView>() {
    private var mostRecentCompletions: Collection<Completion> = emptySet()

    var text by observable(initialText) { _, old, new ->
        if (old != new) {
            logger.fine { "updated search text" }
            mostRecentCompletions = getCompletions()
            logger.fine { "Found ${mostRecentCompletions.size} completions" }
            updateViews(new)
        }
    }

    private fun updateViews(new: String) {
        logger.finest { "updating views" }
        views.forEach { v ->
            v.displaySearchText(new)
            logger.finest { "displaying search text $new" }
            val completions = mostRecentCompletions
            if (completions.isEmpty()) {
                logger.finest { "displaying no completions" }
                v.displayNoCompletions()
            } else {
                v.displayCompletions(completions)
                logger.finest { "displaying completions" }
            }
        }
    }

    override fun viewAdded(view: SearchableListView) {
        view.displaySearchText(text)
        if (mostRecentCompletions.isEmpty()) {
            view.displayNoCompletions()
        } else {
            view.displayCompletions(mostRecentCompletions)
        }
    }

    private fun getCompletions(): Collection<Completion> = completer.completions(text, source.take(maxItems))

    companion object {
        private val DEFAULT_COMPLETION_FACTORY = CompletionFactory<Any> { SimpleCompletion(it.toString()) }
        private val DEFAULT_COMPLETION_STRATEGY = CompletionStrategy.simple
        private val DEFAULT_COMPLETER = ConfiguredCompleter(
            DEFAULT_COMPLETION_STRATEGY,
            DEFAULT_COMPLETION_FACTORY
        )

        val logger by myLogger()
    }
}