/**
 *@author Nikolaus Knop
 */

package hextant.gui

import hextant.base.AbstractController
import hextant.completion.Completer
import hextant.completion.Completion
import hextant.impl.myLogger
import reaktive.event.event
import kotlin.properties.Delegates.observable

class SearchableListController<E : Any>(
    private val completer: Completer<Unit, E>,
    private val maxItems: Int = 10,
    initialText: String = ""
) : AbstractController<SearchableListView<E>>() {
    private var mostRecentCompletions: Collection<Completion<E>> = emptySet()

    private val selectItem = event<E>()

    val selectedItem = selectItem.stream

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
        views {
            displaySearchText(new)
            logger.finest { "displaying search text $new" }
            val completions = mostRecentCompletions
            if (completions.isEmpty()) {
                logger.finest { "displaying no completions" }
                displayNoCompletions()
            } else {
                displayCompletions(completions)
                logger.finest { "displaying completions" }
            }
        }
    }

    override fun viewAdded(view: SearchableListView<E>) {
        view.displaySearchText(text)
        if (mostRecentCompletions.isEmpty()) {
            view.displayNoCompletions()
        } else {
            view.displayCompletions(mostRecentCompletions)
        }
    }

    fun selectCompletion(completion: Completion<E>) {
        logger.info { "${completion.completionText} was selected" }
        selectItem.fire(completion.completion)
        views {
            logger.fine { "closing view" }
            close()
        }
    }

    private fun getCompletions(): Collection<Completion<E>> = completer.completions(Unit, text).take(maxItems)

    companion object {
        val logger by myLogger()
    }

}
