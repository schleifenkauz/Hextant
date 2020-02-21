package hextant.command

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.test.*
import org.jetbrains.spek.api.Spek

internal object CommandSpec : Spek({
    GIVEN("a command") {
        val c = command<CommandSpec, Int> {
            description = "double the passed argument"
            name = "dbl"
            addParameters {
                "x" {
                    description = "The doubled value"
                    ofType<Int>()
                }
            }
            executing { spec, args ->
                spec.double(args[0] as Int)
            }
        }
        ON("executing it") {
            val r = c.execute(CommandSpec, listOf(2))
            IT("should apply the configured function") {
                r shouldMatch equalTo(4)
            }
        }
        ON("executing it with invalid arguments") {
            val err = { c.execute(CommandSpec, listOf("invalid", null)); Unit }
            IT("should throw an ArgumentMismatchException") {
                err shouldMatch throws<ArgumentMismatchException>()
            }
        }
    }
}) {
    private fun double(x: Int): Int = x * 2
}