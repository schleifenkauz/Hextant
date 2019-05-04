/**
 *@author Nikolaus Knop
 */

package hextant.command

import com.nhaarman.mockitokotlin2.*
import hextant.*
import hextant.command.line.*
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.test.matchers.testingContext
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object CommandLineSpec: Spek({
    given("a command line") {
        val context = testingContext {
            get(EditorFactory).register { context -> IntLiteralEditor(context) }
        }
        val command = command<Target, Unit> {
            description = "command"
            shortName = "command"
            name = "command"
            executing { t, _ -> t.execute() }
        }
        val command2 = command<Target, Unit> {
            description = "command with parameter"
            shortName = "command2"
            name = "command2"
            addParameter {
                description = "the parameter"
                this.name = "x"
                ofType<IntLiteral>()
            }
            executing { target, (x) ->
                x as IntLiteral
                target.execute(x.value)
            }
        }
        val possibleCommands = { setOf(command, command2) }
        val target = mock<Target> { on { isApplicable }.then { true } }
        val targets = setOf(target)
        lateinit var intEditor: IntLiteralEditor
        val cl = CommandLine(targets, possibleCommands, context)
        val view = mock<CommandLineView> {
            on { editingArguments(any(), any(), any()) }.then {
                val editors = it.getArgument<List<Editor<*>>>(2)
                intEditor = editors[0] as IntLiteralEditor
                Unit
            }
        }
        inOrder(view, target) {
            on("adding a view") {
                cl.addView(view)
                it("should display the text") {
                    verify(view).editingName("")
                }
            }
            on("editing the name") {
                cl.editName("new")
                it("should display the text") {
                    verify(view).displayText("new")
                }
            }
            on("trying to execute") {
                cl.executeOrExpand()
                it("should do nothing") {
                    verifyNoMoreInteractions()
                }
            }
            on("setting the text to a command without arguments") {
                cl.editName("command")
                it("should display the text") {
                    verify(view).displayText("command")
                }
            }
            on("executing the no-arg command") {
                cl.executeOrExpand()
                it("should execute the command") {
                    verify(target).execute()
                }
                it("should notify the views about the executed command") {
                    verify(view).executed(CommandApplication(command, emptyList(), listOf(Unit)))
                }
                it("should reset") {
                    verify(view).editingName("")
                }
            }
            on("setting the text to a command with arguments") {
                cl.editName("command2")
                it("should display the name") {
                    verify(view).displayText("command2")
                }
            }
            on("expanding") {
                cl.executeOrExpand()
                it("should expand") {
                    verify(view).editingArguments(eq("command2"), eq(command2.parameters), any())
                }
            }
            on("executing when int editor is not ok") {
                cl.executeOrExpand()
                it("should do nothing") {
                    verifyNoMoreInteractions()
                }
            }
            on("executing after making int editor valid") {
                intEditor.setText("123")
                cl.executeOrExpand()
                it("should execute the command") {
                    verify(target).execute(123)
                }
                it("should notify the views about the executed command") {
                    verify(view).executed(CommandApplication(command2, listOf(IntLiteral(123)), listOf(Unit)))
                }
            }
            on("expanding and then resetting") {
                cl.editName("command2")
                cl.executeOrExpand()
                cl.reset()
                it("should edit the name, expand and reset") {
                    verify(view).displayText("command2")
                    verify(view).editingArguments(eq("command2"), eq(command2.parameters), any())
                    verify(view).editingName("")
                }
            }
        }
    }
}) {
    interface Target {
        val isApplicable: Boolean

        fun execute()

        fun execute(x: Int)
    }
}