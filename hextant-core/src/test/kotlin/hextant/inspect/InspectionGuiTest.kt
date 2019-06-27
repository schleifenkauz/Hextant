/**
 * @author Nikolaus Knop
 */

package hextant.inspect

import hextant.*
import hextant.bundle.Bundle
import hextant.command.Commands
import hextant.command.command
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.expr.view.FXIntLiteralEditorView
import hextant.fx.hextantScene
import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import reaktive.value.binding.map
import reaktive.value.now

class InspectionGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = hextantScene(::createContent) { platform -> Context.newInstance(platform) }
        stage.show()
    }

    companion object {
        private fun createContent(context: Context): Parent {
            val inspections = context[Inspections]
            inspections.of(IntLiteralEditor::class).apply {
                register { literal ->
                    inspection(literal) {
                        description = "Reports invalid integer literals"
                        checkingThat(literal.result.map { it.isOk })
                        isSevere(true)
                        message {
                            val t = literal.text.now
                            "'$t' is not a valid integer literal"
                        }
                        addFix {
                            applicableIf { literal.text.now.any(Char::isDigit) }
                            fixingBy {
                                val t = literal.text.now
                                literal.setText(t.filter { it.isDigit() })
                            }
                            description = "Remove all non-digit characters"
                        }
                    }
                }
            }
            val commands = context[Commands]
            commands.of<IntLiteralEditor>().apply {
                register(command<IntLiteralEditor, Unit> {
                    description = "Multiply the integer value"
                    name = "Multiply"
                    shortName = "mul"
                    addParameter {
                        ofType<IntLiteral>()
                        name = "factor"
                        description = "The factor to multiply the value with"
                    }
                    applicableIf { e -> e.result.now.isOk }
                    executing { literal, (f) ->
                        f as IntLiteral
                        val v = literal.result.now.force()
                        literal.setText((v.value * f.value).toString())
                    }
                })
            }
            val editableFactory = context[EditorFactory]
            editableFactory.register(IntLiteral::class) { ctx -> IntLiteralEditor(ctx) }
            val e = IntLiteralEditor(context)
            return FXIntLiteralEditorView(e, Bundle.newInstance())
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(InspectionGuiTest::class.java, *args)
        }
    }
}
