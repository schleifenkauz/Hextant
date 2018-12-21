/**
 * @author Nikolaus Knop
 */

/*
package hextant.core.gui

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.stage.Stage
import hextant.HextantPlatform
import hextant.core.completion.*
import hextant.core.fx.hextantScene
import hextant.core.view.builder.gui.showViewBuilderGui

class SearchableListControllerPopupTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val source = listOf(
                "hextant.World",
                "hextant.core.Hello",
                "hextant.test.Test",
                "hextant.ABC",
                "hextant.ABCD",
                "hextant.ABCDE",
                "hextant.ABCDEF"
            )
            val completer = object : Completer<String> {
                override fun completions(element: String, completionPool: Collection<String>): Set<Completion<String>> {
                    return completionPool.asSequence().filter {
                        val clsName = it.replaceBeforeLast('.', "")
                        CompletionStrategy.simple.isCompletable(element, clsName)
                    }.map { qualified ->
                        val clsName = qualified.replaceBeforeLast('.', "")
                        TextCompletion(qualified, clsName, qualified)
                    }.toSet()
                }
            }
            return Button("Find class").also { b ->
                b.setOnAction {
                    showViewBuilderGui(
                        b,
                        SearchableListControllerPopupTest::class.java.classLoader,
                        HextantPlatform.newInstance()
                    )
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(SearchableListControllerPopupTest::class.java, *args)
        }
    }
}
*/
