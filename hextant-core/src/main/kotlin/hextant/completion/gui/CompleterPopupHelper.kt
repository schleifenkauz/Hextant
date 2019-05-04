/**
 *@author Nikolaus Knop
 */

package hextant.completion.gui

import hextant.completion.Completer
import hextant.fx.show
import javafx.scene.Node

class CompleterPopupHelper<C>(
    private val completer: Completer<C>, private val text: () -> String,
    private val popup: CompletionPopup<C> = CompletionPopup()
) {

    fun show(owner: Node) {
        val input = text()
        val completions = completer.completions(input)
        popup.setCompletions(completions)
        popup.show(owner)
    }

    fun hide() {
        popup.hide()
    }

    val completionChosen get() = popup.completionChosen
}