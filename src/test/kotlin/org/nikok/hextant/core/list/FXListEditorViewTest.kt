package org.nikok.hextant.core.list

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.*
import org.nikok.hextant.core.*
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.fx.hextantScene
import org.nikok.hextant.core.list.FXListEditorView.NumberedCell

class FXListEditorViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.setOnCloseRequest { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val list = EditableList.newInstance<Expr, Editable<Expr>>()
            val platform = HextantPlatform.newInstance()
            val views = platform[EditorViewFactory]
            val editors = platform[EditorFactory]
            views.registerFX<EditableList<*, *>> {
                val editor = editors.resolveEditor(it)
                FXListEditorView(it, editor as ListEditor<*>, platform = platform)
            }
            val view = views.getFXView(list) as FXListEditorView
            view.cellFactory = { NumberedCell() }
            return view
        }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(FXListEditorViewTest::class.java, *args)
        }
    }
}

