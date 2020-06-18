/**
 *@author Nikolaus Knop
 */

package hextant.gui

import hextant.context.Context
import hextant.context.createView
import hextant.gui.DirectoryViewTest.IntEditor
import hextant.main.HextantProjectApplication
import hextant.main.ProjectType
import javafx.scene.Parent
import java.nio.file.Path
import java.nio.file.Paths

class SampleHextantProjectApplication : HextantProjectApplication<IntEditor>() {
    override fun configurationDirectory(): Path = Paths.get("D:", "dev", "hextant")

    override fun createView(editor: IntEditor): Parent = editor.context.createView(editor)

    override fun projectType() = object : ProjectType<IntEditor> {
        override fun createProjectRoot(context: Context): IntEditor = IntEditor(context)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(SampleHextantProjectApplication::class.java)
        }
    }
}