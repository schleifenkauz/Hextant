package org.nikok.hextant.core.list

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.expr.editable.ExpandableExpr
import org.nikok.hextant.core.expr.edited.Expr
import org.nikok.hextant.core.fx.hextantScene
import org.nikok.hextant.core.list.FXListEditorView.NumberedCell
import org.nikok.hextant.core.list.FXListEditorView.Orientation.Vertical

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
            val editor = object : ListEditor<Editable<Expr>>(list, platform) {
                override fun createNewEditable(): Editable<Expr> = ExpandableExpr()
            }
            editor.add(0)
            editor.add(1)
            val view = FXListEditorView(list, editor, Vertical, platform)
            view.cellFactory = { NumberedCell() }
            return view
        }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(FXListEditorViewTest::class.java, *args)
        }
    }
}

