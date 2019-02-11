/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import hextant.HextantPlatform
import hextant.bundle.Bundle
import hextant.core.EditorControlFactory
import hextant.core.configure
import hextant.core.view.FXExpanderView
import hextant.core.view.FXTextEditorView
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.editable.ExpandableExpr
import hextant.expr.view.FXIntLiteralEditorView
import hextant.fx.hextantScene
import hextant.get
import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage

class ExpanderViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.setOnHidden { System.exit(0) }
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val platform = HextantPlatform.configured()
            platform[EditorControlFactory].configure {
                register(EditableIntLiteral::class) { editable, ctx, args ->
                    FXIntLiteralEditorView(
                        editable,
                        ctx,
                        args
                    )
                }
                register(EditableText::class) { editable, ctx, args -> FXTextEditorView(editable, ctx, args) }
            }
            val ex = ExpandableExpr()
            return FXExpanderView(ex, platform, Bundle.newInstance())
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExpanderViewTest::class.java, *args)
        }
    }
}
