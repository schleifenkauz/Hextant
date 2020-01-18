package hextant.core.list

import hextant.*
import hextant.core.editor.ListEditor
import hextant.core.view.ListEditorControl
import hextant.core.view.ListEditorControl.NumberedCell
import hextant.expr.editor.ExprListEditor
import hextant.fx.hextantScene
import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage

class FXListEditorViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(::createContent) { platform -> Context.newInstance(platform) }
        stage.setOnCloseRequest { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent {
            val list = ExprListEditor(context)
            val views = context[EditorControlFactory]
            views.register(ListEditor::class) { editor, args ->
                ListEditorControl(editor, args)
            }
            val view = context.createView(list) as ListEditorControl
            view.cellFactory = { NumberedCell() }
            return view
        }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(FXListEditorViewTest::class.java, *args)
        }
    }
}

