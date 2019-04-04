package hextant.core.editable

import hextant.Err
import hextant.Ok
import hextant.core.instanceOf
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.IntLiteral
import matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.value.now

object EditableTokenSpec : Spek({
    given("an editable int literal") {
        val e = EditableIntLiteral()
        test("it should not be ok") {
            e.isOk shouldBe `false`
        }
        test("e.edited should be null") {
            e.result.now shouldBe instanceOf<Err>()
        }
        on("changing the text to a valid string") {
            e.text.set("123")
            it("should compile it") {
                e.result.now shouldEqual Ok(IntLiteral(123))
            }
            it("should be ok") {
                e.isOk shouldBe `true`
            }
        }
        on("changing the text to an invalid string") {
            e.text.set("invalid")
            it("should not be ok") {
                e.isOk shouldBe `false`
            }
            test("e.edited should be null") {
                e.result.now shouldBe instanceOf<Err>()
            }
        }
    }
})
