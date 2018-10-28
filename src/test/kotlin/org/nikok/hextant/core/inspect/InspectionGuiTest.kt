/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.inspect

import javafx.application.Application
import javafx.scene.Parent
import javafx.stage.Stage
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditableFactory
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.command.command
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.IntLiteral
import org.nikok.hextant.core.expr.view.FXIntLiteralEditorView
import org.nikok.hextant.core.fx.scene
import org.nikok.reaktive.value.now

class InspectionGuiTest : Application() {
    override fun start(stage: Stage) {
        stage.scene = scene(createContent())
        stage.show()
    }

    companion object {
        private fun createContent(): Parent {
            val inspections = HextantPlatform[Public, Inspections]
            inspections.of(EditableIntLiteral::class).apply {
                register { literal ->
                    inspection(literal) {
                        description = "Reports invalid integer literals"
                        checkingThat(literal.isOk)
                        isSevere(true)
                        message {
                            val t = literal.text.now
                            "'$t' is not a valid integer literal"
                        }
                        addFix {
                            applicableIf { literal.text.now.any(Char::isDigit) }
                            fixingBy {
                                val t = literal.text.now
                                literal.text.set(t.filter { it.isDigit() })
                            }
                            description = "Remove all non-digit characters"
                        }
                    }
                }
            }
            val commands = HextantPlatform[Public, Commands]
            commands.of<EditableIntLiteral>().apply {
                register(command<EditableIntLiteral, Unit> {
                    description = "Multiply the integer value"
                    name = "Multiply"
                    shortName = "mul"
                    addParameter {
                        ofType<IntLiteral>()
                        name = "factor"
                        description = "The factor to multiply the value with"
                    }
                    applicableIf { e -> e.isOk.now }
                    executing { literal, (f) ->
                        f as IntLiteral
                        val v = literal.edited.now!!
                        literal.text.set((v.value * f.value).toString())
                    }
                })
            }
            val editableFactory = HextantPlatform[Public, EditableFactory]
            editableFactory.register(IntLiteral::class) { -> EditableIntLiteral() }
            val viewFactory = HextantPlatform[Public, EditorViewFactory]
            viewFactory.registerFX(EditableIntLiteral::class, ::FXIntLiteralEditorView)
            val e = EditableIntLiteral()
            return FXIntLiteralEditorView(e)
        }

        @JvmStatic fun main(args: Array<String>) {
            launch(InspectionGuiTest::class.java, *args)
        }
    }
}