package hextant.core.editable

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
            e.isOk.now shouldBe `false`
        }
        test("e.edited should be null") {
            e.edited.now shouldBe `null`
        }
        on("changing the text to a valid string") {
            e.text.set("123")
            it("should compile it") {
                e.edited.now shouldEqual IntLiteral(123)
            }
            it("should be ok") {
                e.isOk.now shouldBe `true`
            }
        }
        on("changing the text to an invalid string") {
            e.text.set("invalid")
            it("should not be ok") {
                e.isOk.now shouldBe `false`
            }
            test("e.edited should be null") {
                e.edited.now shouldBe `null`
            }
        }
    }
})
