/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import hextant.*
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.bundle.CoreProperties
import hextant.bundle.CoreProperties.serialContext
import hextant.core.editor.ExpanderConfig
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.main.HextantApplication
import hextant.project.editor.*
import hextant.serial.*
import javafx.scene.Parent
import javafx.scene.control.SplitPane
import javafx.scene.layout.Pane
import kserial.KSerial
import java.nio.file.Paths

class DirectoryViewTest : HextantApplication() {
    private val pane = SimpleEditorPane()

    override fun createContext(platform: HextantPlatform): Context = Context.newInstance {
        val serial = KSerial.newInstance()
        set(CoreProperties.serial, serial)
        set(HextantFileManager, HextantFileManagerImpl(serial, get(serialContext)))
        set(Public, ProjectItemExpander.config(), ExpanderConfig<IntEditor>().apply {
            registerInterceptor { text, context -> text.toIntOrNull()?.let { IntEditor(context, text) } }
        })
        set(EditorPane, pane)
    }

    override fun createView(context: Context): Parent {
        val e = DirectoryEditor<Int>(context, FileNameEditor(context, "project"))
        e.items.addAt(0, ProjectItemExpander(context, "file"))
        e.items.addAt(1, ProjectItemExpander(context, "dir"))
        val explorer = ProjectEditorControl(e, Bundle.newInstance())
        return SplitPane(explorer, Pane(pane))
    }

    class IntEditor(context: Context, text: String = "") :
        TokenEditor<Int, TokenEditorView>(context, text), RootEditor<Int> {
        override fun compile(token: String): CompileResult<Int> =
            token.toIntOrNull().okOrErr { "Invalid literal $token" }

        override fun file(): HextantFile<RootEditor<Int>> =
            context[HextantFileManager].get(Paths.get("int.bin"), this)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(DirectoryViewTest::class.java)
        }
    }
}