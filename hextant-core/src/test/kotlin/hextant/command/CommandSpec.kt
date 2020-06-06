package hextant.command

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object CommandSpec : Spek({
    given("a command") {
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
        on("executing it") {
            val r = c.execute(CommandSpec, listOf(2))
            it("should apply the configured function") {
                r shouldMatch equalTo(4)
            }
        }
        on("executing it with invalid arguments") {
            val err = { c.execute(CommandSpec, listOf("invalid", null)); Unit }
            it("should throw an ArgumentMismatchException") {
                err shouldMatch throws<ArgumentMismatchException>()
            }
        }
    }
}) {
    private fun double(x: Int): Int = x * 2
}