/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view

import org.nikok.hextant.Editable
import org.nikok.hextant.EditorView

interface ExpanderView: EditorView {
    fun textChanged(newText: String)

    fun reset()

    fun expanded(newContent: Editable<*>)
}