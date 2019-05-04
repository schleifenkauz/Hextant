/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.Context
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.command.line.CommandLine
import hextant.command.line.FXCommandLineView
import hextant.createView
import hextant.fx.hextantScene
import hextant.impl.SelectionDistributor
import hextant.sample.editor.NameEditor
import javafx.application.Application
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Parent
import javafx.scene.control.SplitPane
import javafx.stage.Stage

class NameEditorGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(::createContent) { platform -> Context.newInstance(platform) }
        stage.setOnHidden { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent {
            val editable = NameEditor(context)
            val nameView = context.createView(editable)
            val cmd = CommandLine.forSelectedEditors(context[Public, SelectionDistributor], context)
            val cmdView = FXCommandLineView(cmd, context, Bundle.newInstance())
            return SplitPane(nameView, cmdView).apply {
                orientation = VERTICAL
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(NameEditorGuiTest::class.java, *args)
        }
    }
}
