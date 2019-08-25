package hextant.gui

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockitokotlin2.*
import hextant.completion.*
import hextant.test.*
import org.jetbrains.spek.api.Spek
import java.util.logging.ConsoleHandler
import java.util.logging.Level

class SearchableListControllerSpec : Spek({
    GIVEN("a searchable list controller with simple completer") {
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
        ON("adding a view") {
            c.addView(viewMock)
            IT("should update the search text of the view") {
                verify(viewMock).displaySearchText("initial")
            }
            IT("should display no completions") {
                verify(viewMock).displayNoCompletions()
            }
        }
        ON("setting the text to be completable") {
            IT("should display all possible completions but not more than max items") {
                c.text = "1"
                verify(viewMock).displayCompletions(check {
                    it.size shouldMatch equalTo(3)
                })
            }
            IT("should update the text") {
                c.text = "1"
                verify(viewMock).displaySearchText("1")
            }
        }
        ON("setting the text not to be completable") {
            c.text = "not completable"
            IT("should display the text") {
                verify(viewMock).displaySearchText("not completable")
            }
            IT("display not completions") {
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