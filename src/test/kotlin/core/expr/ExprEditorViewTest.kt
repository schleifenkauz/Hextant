/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.expr

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.expr.editable.ExpandableExpr
import org.nikok.hextant.core.scene
import org.nikok.hextant.prop.CorePermissions.Public

class ExprEditorViewTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = scene(createContent())
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val views = HextantPlatform.get(Public, EditorViewFactory)
            val expandable = ExpandableExpr()
            return views.getFXView(expandable).node as Parent
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(ExprEditorViewTest::class.java, *args)
        }
    }
}
