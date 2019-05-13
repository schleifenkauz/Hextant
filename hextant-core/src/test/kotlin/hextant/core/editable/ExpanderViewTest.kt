/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import hextant.*
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Public
import hextant.core.view.FXExpanderView
import hextant.expr.editor.ExprExpander
import hextant.expr.editor.IntLiteralEditor
import hextant.expr.view.FXIntLiteralEditorView
import hextant.fx.hextantScene
import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage

class ExpanderViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(::createContent) { platform ->
            Context.newInstance(platform)
        }
        stage.setOnHidden { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent {
            context[Public, EditorControlFactory].configure {
                register(IntLiteralEditor::class, ::FXIntLiteralEditorView)
            }
            val ex = ExprExpander(context)
            return FXExpanderView(ex, Bundle.newInstance())
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExpanderViewTest::class.java, *args)
        }
    }
}
