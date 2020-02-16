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
        val target = mock<Target> { on { isApplicable }.then { true } }
        val targets = setOf(target)
        lateinit var intEditor: IntLiteralEditor
        val source = object : CommandSource {
            override fun focusedTarget(): Any? = null

            override fun selectedTargets(): Collection<Any> = targets

            override fun commandsFor(target: Any): Collection<Command<*, *>> = setOf(command, command2)
        }
        val cl = CommandLine(context, source)
        val view = mock<CommandLineView> {
            on { expanded(any(), any()) }.then {
                if (it.getArgument<Command<*, *>>(0).shortName == "command2") {
                    val editors = it.getArgument<List<Editor<*>>>(1)
                    intEditor = editors[0] as IntLiteralEditor
                }
            }
        }
        inOrder(view, target) {
            ON("adding a view") {
                cl.addView(view)
                IT("should display the text") {
                    verify(view).displayCommandName("")
                }
            }
            ON("editing the name") {
                cl.setCommandName("new")
                IT("should display the text") {
                    verify(view).displayCommandName("new")
                }
            }
            ON("trying to execute") {
                cl.execute()
                IT("should do nothing") {
                    verifyNoMoreInteractions()
                }
            }
            ON("setting the text to a command without arguments") {
                cl.setCommandName("command")
                IT("should display the text") {
                    verify(view).displayCommandName("command")
                }
            }
            ON("executing the no-arg command") {
                cl.execute()
                IT("should execute the command") {
                    verify(target).execute()
                }
                IT("should notify the views about the executed command") {
                    verify(view).addToHistory(command, emptyList(), Unit)
                }
                IT("should reset") {
                    verify(view).reset()
                }
            }
            ON("setting the text to a command with arguments") {
                cl.setCommandName("command2")
                IT("should display the name") {
                    verify(view).displayCommandName("command2")
                }
            }
            ON("expanding") {
                cl.expand()
                IT("should expand") {
                    verify(view).expanded(eq(command2), any())
                }
            }
            ON("executing when int editor is not ok") {
                cl.execute()
                IT("should do nothing") {
                    verifyNoMoreInteractions()
                }
            }
            ON("executing after making int editor valid") {
                intEditor.setText("123")
                cl.execute()
                IT("should execute the command") {
                    verify(target).execute(123)
                }
                IT("should notify the views about the executed command") {
                    verify(view).addToHistory(command2, listOf(IntLiteral(123)), Unit)
                }
            }
            ON("expanding and then resetting") {
                cl.setCommandName("command2")
                cl.expand()
                cl.reset()
                IT("should edit the name, expand and reset") {
                    verify(view).displayCommandName("command2")
                    verify(view).expanded(eq(command2), any())
                    verify(view).reset()
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