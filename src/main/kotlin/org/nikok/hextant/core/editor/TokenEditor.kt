/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.editable.EditableToken
import org.nikok.hextant.core.expr.view.TextEditorView
import org.nikok.reaktive.value.now

/**
 * An editor for tokens
 */
abstract class TokenEditor<T : Any>(
    editable: EditableToken<T>,
    private val platform: HextantPlatform
) : AbstractEditor<EditableToken<T>, TextEditorView>(editable, platform) {
    override fun viewAdded(view: TextEditorView) {
        view.onGuiThread { view.displayText(editable.text.now) }
    }

    /**
     * Set the text on the platform thread and notify the views
     */
    fun setText(new: String) {
        platform.runLater {
            editable.text.set(new)
            views { displayText(new) }
        }
    }
}