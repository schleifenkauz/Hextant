/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editable

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.configure
import org.nikok.hextant.core.expr.editable.*
import org.nikok.hextant.core.expr.editor.ExprExpander
import org.nikok.hextant.core.expr.view.FXIntLiteralEditorView
import org.nikok.hextant.core.expr.view.FXTextEditorView
import org.nikok.hextant.core.fx.scene
import org.nikok.hextant.core.view.FXExpanderView

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
            return FXExpanderView(ex, ExprExpander(ex))
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExpanderViewTest::class.java, *args)
        }
    }
}
