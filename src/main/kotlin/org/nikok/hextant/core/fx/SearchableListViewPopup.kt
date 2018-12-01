/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.fx

import javafx.scene.control.Label
import javafx.scene.control.PopupControl
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.nikok.hextant.core.completion.Completion
import org.nikok.hextant.core.gui.SearchableListController
import org.nikok.hextant.core.gui.SearchableListView

class SearchableListViewPopup(
    private val controller: SearchableListController<*>,
    noCompletionsText: String = DEFAULT_NO_COMPLETIONS_TEXT
) : PopupControl(),
    SearchableListView {
    private val searchBar = SearchBar()

    private val noCompletionsLabel = Label(noCompletionsText)

    private val root = HBox(searchBar, noCompletionsLabel)

    private val completionsBox = VBox().apply {
        styleClass.add("searchable-list-completions")
    }

    init {
        initGui()
        listenForSearchTextChange()
    }

    private fun initGui() {
        scene = hextantScene(root)
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

    override fun displayCompletions(completions: Collection<Completion>) {
        completionsBox.children.clear()
        root.children[1] = completionsBox
        for (completion in completions) {
            val node = completion.createNode()
            completionsBox.children.add(node)
        }
    }

    override fun displayNoCompletions() {
        root.children[1] = noCompletionsLabel
    }

    companion object {
        private const val DEFAULT_NO_COMPLETIONS_TEXT = "No completions available"
    }
}