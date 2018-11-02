/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditableFactory
import org.nikok.hextant.core.command.line.CommandLine
import org.nikok.hextant.core.command.line.CommandLine.State.EditingName
import org.nikok.hextant.core.expr.editable.EditableIntLiteral
import org.nikok.hextant.core.expr.edited.IntLiteral
import org.nikok.reaktive.value.now

internal object CommandLineSpec: Spek({
    data class Target(val isApplicable: Boolean)
    given("a command line") {
        val possibleCommands = mutableSetOf<Command<*, *>>()
        val targets = mutableSetOf<Target>()
        val editableInts = mutableListOf<EditableIntLiteral>()
        EditableFactory.newInstance(CommandLineSpec.javaClass.classLoader).apply {
            register(IntLiteral::class) { -> EditableIntLiteral().also { editableInts.add(it) } }
        }
        val platform = HextantPlatform.newInstance()
        val cl = CommandLine({ possibleCommands }, { targets }, platform)
        it("should be editing the name") {
            cl.state.now shouldMatch equalTo(EditingName)
        }
        on("asking for the edited command") {
            val error = { cl.editedCommand(); Unit }
            it("should throw a ISE") {
                error shouldMatch throws<IllegalStateException>()
            }
        }
        on("asking for the edited args") {
            val error = { cl.editableArgs(); Unit }
            it("should throw a NPE") {
                error shouldMatch throws<IllegalStateException>()
            }
        }
        test("the text should be an empty string") {
            cl.text.now shouldMatch equalTo("")
        }
        on("setting the text") {
            cl.setText("new")
            it("should set the text") {
                cl.text.now shouldMatch equalTo("new")
            }
        }
        on("trying to execute") {
            cl.executeOrExpand()
            it("should do nothing") {
                cl.state.now shouldMatch equalTo(EditingName)
                cl.text.now shouldMatch equalTo("new")
            }
        }
        on("setting the text to a command without arguments and then executing") {
            var executed = false
            possibleCommands.add(command<Target, Unit> {
                description = "2"
                shortName = "2"
                name = "2"
                executing { _, _ -> executed = true }
            })
            targets.add(Target(true))
            cl.setText("2")
            cl.executeOrExpand()
            it("should execute the command") {
                executed shouldMatch equalTo(true)
            }
            it("should reset the text") {
                cl.text.now shouldMatch equalTo("")
            }
            it("should reset leave the equal to EditingName") {
                cl.state.now shouldMatch equalTo(EditingName)
            }
        }
        on("setting the text to a command with arguments and then expanding") {

        }
    }
})