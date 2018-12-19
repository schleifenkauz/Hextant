/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import javafx.application.Application
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Parent
import javafx.scene.control.SplitPane
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.command.line.CommandLine
import org.nikok.hextant.core.command.line.FXCommandLineView
import org.nikok.hextant.core.fx.hextantScene
import org.nikok.hextant.get
import org.nikok.hextant.sample.editable.EditableName

class NameEditorGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.setOnHidden { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val platform = HextantPlatform.newInstance()
            val editable = EditableName()
            val nameView = platform[EditorViewFactory].getFXView(editable)
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
