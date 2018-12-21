/**
 *@author Nikolaus Knop
 */

package hextant.gui

import hextant.completion.Completion

interface SearchableListView<T> {
    fun displaySearchText(new: String)

    fun displayCompletions(completions: Collection<Completion<T>>)

    fun displayNoCompletions()

    fun close()
}