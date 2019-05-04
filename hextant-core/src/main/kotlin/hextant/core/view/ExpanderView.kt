/**
 * @author Nikolaus Knop
 */

package hextant.core.view

import hextant.Editor

interface ExpanderView {
    fun expanded(editor: Editor<*>)

    fun reset()

    fun displayText(text: String)
}