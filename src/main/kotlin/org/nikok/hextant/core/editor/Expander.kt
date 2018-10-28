/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.view.ExpanderView
import org.nikok.reaktive.value.now

abstract class Expander<out E : Editable<*>>(
    editable: Expandable<*, E>
) : AbstractEditor<Expandable<*, @UnsafeVariance E>, ExpanderView>(editable) {
    private fun ExpanderView.handleState() {
        if (editable.isExpanded.now) {
            expanded(editable.editable.now!!)
        } else {
            reset()
        }
    }

    override fun viewAdded(view: ExpanderView) {
        view.onGuiThread {
            view.handleState()
        }
    }

    protected abstract fun expand(text: String): E?

    fun expand() {
        HextantPlatform.runLater {
            check(!editable.isExpanded.now) { "Expander is already expanded" }
            val content = expand(editable.text.now) ?: return@runLater
            editable.setContent(content)
            views {
                handleState()
            }
        }
    }

    fun reset() {
        HextantPlatform.runLater {
            check(editable.isExpanded.now) { "Expander is not expanded" }
            editable.setText("")
            views {
                handleState()
            }
        }
    }

    fun setText(new: String) {
        HextantPlatform.runLater {
            editable.setText(new)
            views {
                textChanged(new)
            }
        }
    }
}