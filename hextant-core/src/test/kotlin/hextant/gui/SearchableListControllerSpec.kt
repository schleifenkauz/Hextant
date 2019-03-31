package hextant.gui

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockitokotlin2.*
import hextant.completion.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import java.util.logging.ConsoleHandler
import java.util.logging.Level

class SearchableListControllerSpec : Spek({
    given("A searchable list controller with simple completer") {
        val logger = SearchableListController.logger
        logger.level = Level.ALL
        val handler = ConsoleHandler().also { it.level = Level.ALL }
        logger.addHandler(handler)
        val source = setOf(12, 12, 13, 14)
        val completer = ConfiguredCompleter(
            CompletionStrategy.simple,
            CompletionFactory.simple(),
            { source }
        )
        val c = SearchableListController(completer, 3, "initial")
        val viewMock = mock<SearchableListView<Int>>()
        on("adding a view") {
            c.addView(viewMock)
            it("should update the search text of the view") {
                verify(viewMock).displaySearchText("initial")
            }
            it("should display no completions") {
                verify(viewMock).displayNoCompletions()
            }
        }
        on("setting the text to be completable") {
            it("should display all possible completions but not more than max items") {
                c.text = "1"
                verify(viewMock).displayCompletions(check {
                    it.size shouldMatch equalTo(3)
                })
            }
            it("should update the text") {
                c.text = "1"
                verify(viewMock).displaySearchText("1")
            }
        }
        on("setting the text not to be completable") {
            c.text = "not completable"
            it("should display the text") {
                verify(viewMock).displaySearchText("not completable")
            }
            it("display not completions") {
                verify(viewMock, times(2)).displayNoCompletions()
            }
        }
        verifyNoMoreInteractions(viewMock)
        afterGroup {
            logger.removeHandler(handler)
            logger.level = Level.INFO
        }
    }
})