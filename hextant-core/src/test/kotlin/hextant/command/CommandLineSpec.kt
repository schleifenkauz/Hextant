/**
 *@author Nikolaus Knop
 */

package hextant.command

import com.nhaarman.mockitokotlin2.*
import hextant.*
import hextant.command.line.*
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.test.*
import org.jetbrains.spek.api.Spek

internal object CommandLineSpec: Spek({
    GIVEN("a command line") {
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
            ON("adding a view") {
                cl.addView(view)
                IT("should display the text") {
                    verify(view).editingName("")
                }
            }
            ON("editing the name") {
                cl.editName("new")
                IT("should display the text") {
                    verify(view).displayText("new")
                }
            }
            ON("trying to execute") {
                cl.executeOrExpand()
                IT("should do nothing") {
                    verifyNoMoreInteractions()
                }
            }
            ON("setting the text to a command without arguments") {
                cl.editName("command")
                IT("should display the text") {
                    verify(view).displayText("command")
                }
            }
            ON("executing the no-arg command") {
                cl.executeOrExpand()
                IT("should execute the command") {
                    verify(target).execute()
                }
                IT("should notify the views about the executed command") {
                    verify(view).executed(CommandApplication(command, emptyList(), listOf(Unit)))
                }
                IT("should reset") {
                    verify(view).editingName("")
                }
            }
            ON("setting the text to a command with arguments") {
                cl.editName("command2")
                IT("should display the name") {
                    verify(view).displayText("command2")
                }
            }
            ON("expanding") {
                cl.executeOrExpand()
                IT("should expand") {
                    verify(view).editingArguments(eq("command2"), eq(command2.parameters), any())
                }
            }
            ON("executing when int editor is not ok") {
                cl.executeOrExpand()
                IT("should do nothing") {
                    verifyNoMoreInteractions()
                }
            }
            ON("executing after making int editor valid") {
                intEditor.setText("123")
                cl.executeOrExpand()
                IT("should execute the command") {
                    verify(target).execute(123)
                }
                IT("should notify the views about the executed command") {
                    verify(view).executed(CommandApplication(command2, listOf(IntLiteral(123)), listOf(Unit)))
                }
            }
            ON("expanding and then resetting") {
                cl.editName("command2")
                cl.executeOrExpand()
                cl.reset()
                IT("should edit the name, expand and reset") {
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