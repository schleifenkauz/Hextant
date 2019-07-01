/**
 *@author Nikolaus Knop
 */

package hextant.completion.gui

import hextant.completion.Completer
import hextant.fx.show
import javafx.scene.Node

/**
 * Helper for JavaFx completion popups
 * @property completer the completer used to get the completions
 */
class CompleterPopupHelper<C>(
    var completer: Completer<C>, private val text: () -> String,
    private val popup: CompletionPopup<C> = CompletionPopup()
) {
    /**
     * Show the popup on the given owner node
     */
    fun show(owner: Node) {
        val input = text()
        val completions = completer.completions(input)
        popup.setCompletions(completions)
        popup.show(owner)
    }

    /**
     * Hide the popup
     */
    fun hide() {
        popup.hide()
    }

    /**
     * Stream emitting completions when they are chosen
     */
    val completionChosen get() = popup.completionChosen
}