/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.*
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.core.editor.ExpanderConfig
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.fx.ModifierValue.DOWN
import hextant.fx.menuBar
import hextant.fx.shortcut
import hextant.main.HextantApplication
import hextant.project.editor.*
import hextant.serial.HextantFileManager
import hextant.serial.HextantFileManagerImpl
import hextant.serial.SerialProperties.projectRoot
import hextant.serial.SerialProperties.serial
import hextant.serial.SerialProperties.serialContext
import javafx.scene.Parent
import javafx.scene.control.SplitPane
import javafx.scene.input.KeyCode.S
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import kserial.createOutput
import java.nio.file.Paths

class DirectoryViewTest : HextantApplication() {
    private val pane = SimpleEditorPane()

    override fun createContext(platform: HextantPlatform): Context = Context.newInstance {
        set(HextantFileManager, HextantFileManagerImpl(get(serial), get(serialContext)))
        set(Public, ProjectItemExpander.config(), ExpanderConfig<IntEditor>().apply {
            registerInterceptor { text, context -> text.toIntOrNull()?.let { IntEditor(context, text) } }
        })
        set(projectRoot, Paths.get("D:", "dev", "hextant", "sample-project"))
        set(EditorPane, pane)
    }

    override fun createView(context: Context): Parent {
        val e = DirectoryEditor<Int>(context, FileNameEditor(context, "project"))
        e.items.addAt(0, ProjectItemExpander(context, "file"))
        e.items.addAt(1, ProjectItemExpander(context, "file"))
        val explorer = ProjectEditorControl(e, Bundle.newInstance())
        val menu = menuBar {
            menu("File") {
                item("Save", shortcut(S) { control(DOWN) }) {
                    val path = context[projectRoot].resolve("project")
                    val output = context[serial].createOutput(path, context[serialContext])
                    output.writeObject(e)
                }
            }
        }
        return VBox(menu, SplitPane(explorer, Pane(pane)))
    }

    class IntEditor(context: Context, text: String = "") :
        TokenEditor<Int, TokenEditorView>(context, text) {
        override fun compile(token: String): CompileResult<Int> =
            token.toIntOrNull().okOrErr { "Invalid literal $token" }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(DirectoryViewTest::class.java)
        }
    }
}