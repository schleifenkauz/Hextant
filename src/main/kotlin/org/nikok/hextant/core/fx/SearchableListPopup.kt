/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.fx

import javafx.scene.control.*
import javafx.scene.input.KeyCode.ENTER
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.nikok.hextant.core.completion.Completion
import org.nikok.hextant.core.gui.SearchableListController
import org.nikok.hextant.core.gui.SearchableListView

class SearchableListPopup<T : Any>(
    private val controller: SearchableListController<T>,
    noCompletionsText: String = DEFAULT_NO_COMPLETIONS_TEXT
) : PopupControl(),
    SearchableListView<T> {
    private val searchBar = SearchBar()

    private val noCompletionsLabel = Label(noCompletionsText).apply { blackBackground() }

    private fun Region.blackBackground() {
        background = Background(BackgroundFill(Color.BLACK, CornerRadii(5.0), insets))
        if (this is Label) {
            textFill = Color.WHITE
        }
    }

    private val root = VBox(5.0, searchBar, noCompletionsLabel)

    private val completionsBox = VBox().apply {
        styleClass.add("searchable-list-completions")
        blackBackground()
    }

    init {
        scene.root = root
        scene.initHextantScene()
        listenForSearchTextChange()
    }

    private fun listenForSearchTextChange() {
        controller.addView(this)
        searchBar.textProperty().addListener { _, _, new ->
            controller.text = new
        }
    }

    override fun displaySearchText(new: String) {
        searchBar.text = new
    }

    override fun displayCompletions(completions: Collection<Completion<T>>) {
        completionsBox.children.clear()
        root.children[1] = completionsBox
        for (completion in completions) {
            val node = completion.createNode()
            completionsBox.isFillWidth = true
            node.styleClass.add("completion")
            addSelectionHandlers(node, completion)
            completionsBox.children.add(node)
        }
    }

    private fun addSelectionHandlers(node: Control, completion: Completion<T>) {
        node.setOnMouseClicked { evt ->
            controller.selectCompletion(completion)
            evt.consume()
        }
        node.setOnKeyPressed { evt ->
            if (evt.code == ENTER) {
                controller.selectCompletion(completion)
                evt.consume()
            }
        }
    }

    override fun displayNoCompletions() {
        root.children[1] = noCompletionsLabel
    }

    override fun close() {
        hide()
    }

    companion object {
        private const val DEFAULT_NO_COMPLETIONS_TEXT = "No completions available"
    }
}