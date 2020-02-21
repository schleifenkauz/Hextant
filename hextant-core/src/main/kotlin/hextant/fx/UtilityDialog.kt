/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.Context
import hextant.bundle.Internal
import hextant.impl.Stylesheets
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.ButtonType
import javafx.stage.Window

/**
 * A Hextant Utility dialog
 * Adds style class "utility-dialog-pane" to its dialog pane
 * @constructor
 * @param title the title of this dialog, is placed in the window frame
 * @param headerText the header text of this dialog, is placed at the top of the content
 * @param content the content of this dialog, is placed in the middle
 * @param owner the [Window] that owns this dialog
 */
class UtilityDialog(
    context: Context, title: String, headerText: String, content: Node, owner: Window
) : Alert(CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL) {
    init {
        initOwner(owner)
        context[Internal, Stylesheets].apply(dialogPane.scene)
        dialogPane.styleClass.add("utility-dialog-pane")
        this.headerText = headerText
        this.title = title
        dialogPane.content = content
        addButtonClasses()
    }

    private fun addButtonClasses() {
        val ok = dialogPane.lookupButton(ButtonType.OK)
        ok.styleClass.add("ok-button")
    }

    /**
     * Builder for [UtilityDialog]s
     */
    interface Builder {
        /**
         * Refer to constructor doc of [UtilityDialog]
         */
        var title: String
        /**
         * Refer to constructor doc of [UtilityDialog]
         */
        var headerText: String

        /**
         * Refer to constructor doc of [UtilityDialog]
         */
        var content: Node

        /**
         * Refer to constructor doc of [UtilityDialog]
         */
        var owner: Window
    }

    private class BuilderImpl(private val context: Context) : Builder {
        override lateinit var title: String
        override lateinit var headerText: String
        override lateinit var content: Node
        override lateinit var owner: Window

        fun build() = UtilityDialog(context, title, headerText, content, owner)
    }

    companion object {
        /**
         * @return a [UtilityDialog] built with [block]
         */
        operator fun invoke(context: Context, block: Builder.() -> Unit): UtilityDialog =
            BuilderImpl(context).apply(block).build()

        /**
         * Show a [UtilityDialog] build with [block] and return the clicked button
         */
        fun show(context: Context, block: Builder.() -> Unit): ButtonType? =
            UtilityDialog(context, block).showAndWait().orElse(null)
    }
}