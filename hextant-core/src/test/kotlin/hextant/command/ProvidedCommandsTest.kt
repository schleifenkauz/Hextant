/**
 *@author Nikolaus Knop
 */

package hextant.command

import com.natpryce.hamkrest.should.shouldMatch
import hextant.command.Command.Category
import hextant.command.meta.CommandParameter
import hextant.command.meta.ProvideCommand
import hextant.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProvidedCommandsTest {
    private val commands = Commands.newInstance()

    @Test fun `command function with no further configuration`() {
        val t = Target1()
        val cmd = commands.applicableOn(t).first()
        cmd.name shouldEqual "default"
        cmd.shortName shouldEqual "default"
        cmd.category shouldEqual null
        cmd.description shouldEqual "No description provided"
        cmd.parameters.size shouldEqual 1
        val p = cmd.parameters[0]
        p.name shouldEqual "x"
        p.description shouldEqual "No description provided"
        p.nullable shouldBe `false`
        p.type shouldEqual Int::class
        cmd.execute(t, listOf(1)) shouldEqual 1
    }

    @Test fun `command with nullable parameter`() {
        val t = Target2()
        val cmd = commands.applicableOn(t).first()
        val p = cmd.parameters[0]
        p.nullable shouldBe `true`
        p.type shouldEqual String::class
    }

    @Test fun `command throwing exception`() {
        val t = Target3()
        val cmd = commands.applicableOn(t).first()
        assertThrows<AssertionError> { cmd.execute(t, emptyList()) }
    }

    @Test fun `command with configuration`() {
        val t = Target4()
        val cmd = commands.applicableOn(t).first()
        cmd.name shouldEqual "Some random command"
        cmd.shortName shouldEqual "cmd"
        cmd.category shouldEqual Category.withName("Unnecessary")
        cmd.description shouldEqual "Does nothing"
        val p = cmd.parameters[0]
        p.name shouldEqual "arg"
        p.description shouldEqual "Completely useless"
        p.nullable shouldEqual false
        p.type shouldEqual Int::class
        cmd.execute(t, listOf(1)) shouldEqual 1
    }

    @Test fun `command with receiver should fail`() {
        val t = Target5()
        commands.applicableOn(t) shouldMatch isEmpty
    }

    @Test fun `special name should not be a problem`() {
        val t = Target6()
        val cmd = commands.applicableOn(t).first()
        cmd.name shouldEqual "some random name"
    }

    private class Target1 {
        @ProvideCommand
        fun default(x: Int) = x
    }

    private class Target2 {
        @ProvideCommand
        fun default(x: String?) {
        }
    }

    private class Target3 {
        @ProvideCommand
        fun error(): Nothing = throw AssertionError("ERROR")
    }

    private class Target4 {
        @ProvideCommand(
            name = "Some random command",
            shortName = "cmd",
            category = "Unnecessary",
            description = "Does nothing"
        )
        fun random(@CommandParameter(name = "arg", description = "Completely useless") x: Int) = x
    }

    private class Target5 {
        @ProvideCommand
        fun Int.withReceiver() {
        }
    }

    private class Target6 {
        @ProvideCommand
        fun `some random name`() {
        }
    }
}

