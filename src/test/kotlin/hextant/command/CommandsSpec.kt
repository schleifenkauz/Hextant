package hextant.command

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.should.shouldMatch
import hextant.HextantPlatform
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object CommandsSpec: Spek({
    given("commands") {
        val platform = HextantPlatform.configured()
        afterGroup { platform.exit() }
        val cs = Commands.newInstance()
        val registrar = cs.of<ICommandTarget>()
        val command = command<ICommandTarget, Unit> {
            description = "another command"
            name = "cmd"
            executing { _, _ -> println("Executed another command") }
        }
        on("registering a new command") {
            registrar.register(command)
            test("the new command should be available") {
                registrar.commands shouldMatch Matcher(Set<*>::contains, command)
            }
        }
        it("should also use the commands of superclasses") {
            cs.of<CommandTarget>().commands shouldMatch Matcher(Set<*>::contains, command)
        }
        on("registering a command that is never applicable") {
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
        it("should cache registrars") {
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