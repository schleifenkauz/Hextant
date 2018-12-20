package org.nikok.hextant.core.list

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.*
import org.nikok.hextant.core.EditorControlFactory
import org.nikok.hextant.core.expr.editable.EditableExprList
import org.nikok.hextant.core.fx.hextantScene
import org.nikok.hextant.core.list.FXListEditorView.NumberedCell
import org.nikok.hextant.core.register

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
            views.register<EditableList<*, *>> { editable, ctx ->
                val editor = ctx.getEditor(editable)
                FXListEditorView(editable, editor as ListEditor<*>, ctx)
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

