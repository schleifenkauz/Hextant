/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder

import javafx.application.Application
import javafx.scene.Parent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.nikok.hextant.*
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.base.AbstractEditable
import org.nikok.hextant.core.base.AbstractEditor
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.hextantScene
import org.nikok.reaktive.value.*

class EditableWhile : AbstractEditable<Nothing>() {
    override val edited: ReactiveValue<Nothing?> = reactiveValue("nothing", null)
    override val isOk: ReactiveBoolean
        get() = reactiveValue("always ok", true)
}

class WhileEditor(
    editable: EditableWhile,
    platform: HextantPlatform
) : AbstractEditor<EditableWhile, EditorView>(editable, platform)


internal class FXEditorViewBuilderTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(createContent())
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val editableIntLiteral = EditableIntLiteral()
            val text = EditableText()
            val platform = HextantPlatform.newInstance()
            val views = platform[EditorViewFactory]
            val editorView = fxEditorView<Editable<EditableWhile>>(platform, "style") {
                line {
                    keyword("while")
                    operator("(")
                    view(editableIntLiteral)
                    operator(")")
                    operator("{")
                }
                indented {
                    line {
                        view(text)
                    }
                }
            }(EditableWhile()).node
            val another = EditableIntLiteral().let {
                views.getFXView(it)
            }
            return VBox(editorView, another.node)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            launch(FXEditorViewBuilderTest::class.java, *args)
        }
    }
}
