/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import hextant.Editable
import hextant.EditorView
import hextant.completion.Completion

interface ExpanderView: EditorView {
    fun textChanged(newText: String)

    fun reset()

    fun expanded(newContent: Editable<*>)

    fun suggestCompletions(completions: Set<Completion<String>>)
}