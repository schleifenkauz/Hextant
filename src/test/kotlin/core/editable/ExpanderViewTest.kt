/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editable

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.*
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.editable.ExpandableExpr
import org.nikok.hextant.core.expr.editor.ExprExpander
import org.nikok.hextant.core.expr.view.FXIntLiteralEditorView
import org.nikok.hextant.core.view.FXExpanderView
import org.nikok.hextant.core.view.FXTextEditorView
import org.nikok.hextant.prop.CorePermissions.Public

class ExpanderViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = scene(createContent())
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            HextantPlatform[Public, EditorViewFactory].configure {
                registerFX(EditableIntLiteral::class) { FXIntLiteralEditorView(it) }
                registerFX(EditableText::class) { FXTextEditorView(it) }
            }
            val ex = ExpandableExpr()
            return FXExpanderView(ex) { v -> ExprExpander(ex, v) }
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExpanderViewTest::class.java, *args)
        }
    }
}
