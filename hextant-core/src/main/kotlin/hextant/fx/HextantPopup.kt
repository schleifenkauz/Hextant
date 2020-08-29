/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.context.Context
import javafx.scene.Parent
import javafx.stage.Popup

internal open class HextantPopup(context: Context) : Popup() {
    constructor(context: Context, root: Parent) : this(context) {
        scene.root = root
    }

    init {
        context[Stylesheets].manage(scene)
        isHideOnEscape = true
        isAutoHide = true
        scene.registerShortcuts {
            on("ESCAPE") {
                hide()
            }
        }
    }
}