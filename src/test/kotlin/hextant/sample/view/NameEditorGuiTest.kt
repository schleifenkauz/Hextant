/**
 *@author Nikolaus Knop
 */

package hextant.sample.view

import hextant.HextantPlatform
import hextant.core.command.line.CommandLine
import hextant.core.command.line.FXCommandLineView
import hextant.core.fx.hextantScene
import hextant.createView
import hextant.sample.editable.EditableName
import javafx.application.Application
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Parent
import javafx.scene.control.SplitPane
import javafx.stage.Stage

class NameEditorGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.setOnHidden { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val platform = HextantPlatform.configured()
            val editable = EditableName()
            val nameView = platform.createView(editable)
            val cmd = CommandLine.forSelectedEditors(platform)
            val cmdView = FXCommandLineView(cmd, platform)
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
