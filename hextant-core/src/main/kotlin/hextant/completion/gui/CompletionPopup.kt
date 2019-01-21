/**
 *@author Nikolaus Knop
 */

package hextant.completion.gui

import hextant.completion.Completion
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.layout.VBox
import javafx.stage.Popup
import reaktive.event.event

class CompletionPopup<C> : Popup() {
    private val choose = event<Completion<C>>()
    val completionChosen = choose.stream

    init {
        isAutoHide = true
    }

    fun setCompletions(completions: Set<Completion<C>>) {
        val vbox = VBox()
        vbox.styleClass.add("completions")
        for (c in completions) {
            addChoice(c, vbox)
        }
        scene.root = vbox
    }

    private fun addChoice(c: Completion<C>, vbox: VBox) {
        val n = c.createNode()
        vbox.children.add(n)
        n.setOnKeyPressed {
            if (it.code == ENTER) {
                choose(c)
                it.consume()
            }
        }
        n.setOnMouseClicked {
            if (it.clickCount > 1) {
                choose(c)
                it.consume()
            }
        }
    }

    private fun choose(c: Completion<C>) {
        choose.fire(c)
        ownerNode.requestFocus()
    }

}

