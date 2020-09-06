/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.context.Context
import javafx.beans.InvalidationListener
import javafx.scene.Parent
import javafx.stage.Popup

/**
 * Utility class for [Popup]s with common look and feel.
 */
open class HextantPopup(context: Context) : Popup() {
    constructor(context: Context, root: Parent) : this(context) {
        scene.root = root
    }

    private val focusListener = InvalidationListener { hide() }

    init {
        context[Stylesheets].manage(scene)
        autoHide()
    }

    private fun autoHide() {
        ownerWindowProperty().addListener { _, old, new ->
            old?.scene?.focusOwnerProperty()?.removeListener(focusListener)
            new?.scene?.focusOwnerProperty()?.addListener(focusListener)
        }
        isHideOnEscape = true
        isAutoHide = true
        scene.registerShortcuts {
            on("ESCAPE") {
                hide()
            }
        }
    }
}