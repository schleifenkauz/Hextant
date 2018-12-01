/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.gui

import org.nikok.hextant.core.completion.Completion

interface SearchableListView {
    fun displaySearchText(new: String)

    fun displayCompletions(completions: Collection<Completion>)

    fun displayNoCompletions()
}