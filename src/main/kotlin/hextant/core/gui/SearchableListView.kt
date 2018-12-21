/**
 *@author Nikolaus Knop
 */

package hextant.core.gui

import hextant.core.completion.Completion

interface SearchableListView<T> {
    fun displaySearchText(new: String)

    fun displayCompletions(completions: Collection<Completion<T>>)

    fun displayNoCompletions()

    fun close()
}