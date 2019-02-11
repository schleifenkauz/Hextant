package hextant.core.list

import hextant.*
import hextant.core.EditorControlFactory
import hextant.core.list.FXListEditorView.NumberedCell
import hextant.core.register
import hextant.expr.editable.EditableExprList
import hextant.fx.hextantScene
import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage

class FXListEditorViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.setOnCloseRequest { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val list = EditableExprList()
            val platform = HextantPlatform.configured()
            val views = platform[EditorControlFactory]
            views.register<EditableList<*, *>> { editable, ctx, args ->
                val editor = ctx.getEditor(editable)
                FXListEditorView(editable, ctx, editor as ListEditor<*>, bundle = args)
            }
            val view = platform.createView(list) as FXListEditorView
            view.cellFactory = { NumberedCell() }
            return view
        }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(FXListEditorViewTest::class.java, *args)
        }
    }
}

