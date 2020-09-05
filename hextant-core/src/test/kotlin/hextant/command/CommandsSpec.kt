package hextant.command

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object CommandsSpec : Spek({
    given("commands") {
        val commands = Commands.newInstance()
        val command = command<ICommandTarget, Unit> {
            description = "another command"
            name = "cmd"
            executing { _, _ -> println("Executed another command") }
        }
        on("registering a new command") {
            commands.register(command)
            test("the new command should be available") {
                commands.applicableOn(CommandTarget) shouldMatch Matcher(Collection<*>::contains, command)
            }
        }
        it("should also use the commands of superclasses") {
            commands.applicableOn(CommandTarget) shouldMatch Matcher(Collection<*>::contains, command)
        }
        on("registering a command that is never applicable") {
            val notApplicable = command<ICommandTarget, Unit> {
                description = "never applicable"
                name = "x"
                applicableIf { false }
                executing { _, _ -> }
            }
            commands.register(notApplicable)
            test("the commands applicable on the CommandTarget should not contain the new command") {
                val applicableCommands = commands.applicableOn(CommandTarget)
                applicableCommands shouldMatch hasSize(equalTo(1))
                applicableCommands shouldMatch !Matcher(Collection<*>::contains, notApplicable)
            }
        }
    }
}) {
    interface ICommandTarget {
        fun c()
    }

    object CommandTarget : ICommandTarget {
        override fun c() {}
    }
}