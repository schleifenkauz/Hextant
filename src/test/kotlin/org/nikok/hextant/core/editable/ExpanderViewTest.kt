/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editable

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.configure
import org.nikok.hextant.core.expr.editable.*
import org.nikok.hextant.core.expr.view.FXIntLiteralEditorView
import org.nikok.hextant.core.expr.view.FXTextEditorView
import org.nikok.hextant.core.fx.hextantScene
import org.nikok.hextant.core.view.FXExpanderView
import org.nikok.hextant.get

class ExpanderViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val platform = HextantPlatform.INSTANCE.copy { }
            platform[EditorViewFactory].configure {
                registerFX(EditableIntLiteral::class) { FXIntLiteralEditorView(it, platform) }
                registerFX(EditableText::class) { FXTextEditorView(it, platform) }
            }
            val ex = ExpandableExpr()
            return FXExpanderView(ex, platform)
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExpanderViewTest::class.java, *args)
        }
    }
}
