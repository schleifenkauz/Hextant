package hextant.command

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.should.shouldMatch
import hextant.test.*
import org.jetbrains.spek.api.Spek

internal object CommandsSpec: Spek({
    GIVEN("commands") {
        val cs = Commands.newInstance()
        val registrar = cs.of<ICommandTarget>()
        val command = command<ICommandTarget, Unit> {
            description = "another command"
            name = "cmd"
            executing { _, _ -> println("Executed another command") }
        }
        ON("registering a new command") {
            registrar.register(command)
            test("the new command should be available") {
                registrar.commands shouldMatch Matcher(Set<*>::contains, command)
            }
        }
        IT("should also use the commands of superclasses") {
            cs.of<CommandTarget>().commands shouldMatch Matcher(Set<*>::contains, command)
        }
        ON("registering a command that is never applicable") {
            val notApplicable = command<ICommandTarget, Unit> {
                description = "never applicable"
                name = "x"
                applicableIf { false }
                executing { _, _ ->  }
            }
            registrar.register(notApplicable)
            test("the commands applicable on the CommandTarget should not contain the new command") {
                val applicableCommands = registrar.commandsFor(CommandTarget)
                applicableCommands shouldMatch hasSize(equalTo(1))
                applicableCommands shouldMatch !Matcher(Set<*>::contains, notApplicable)
            }
        }
        IT("should cache registrars") {
            cs.of<CommandTarget>() shouldMatch equalTo(cs.of(CommandTarget::class))
        }
    }
}) {
    interface ICommandTarget {
        fun c()
    }

    object CommandTarget: ICommandTarget {
        override fun c() {}
    }
}