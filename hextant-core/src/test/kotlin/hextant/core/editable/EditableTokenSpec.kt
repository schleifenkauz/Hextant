package hextant.core.editable

import hextant.Ok
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.isOk
import hextant.test.matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.value.now

object EditableTokenSpec : Spek({
    given("an editable int literal") {
        val context = testingContext()
        val e = IntLiteralEditor(context)
        test("it should not be ok") {
            e.result.now.isOk shouldBe `false`
        }
        test("e.result should be an Err") {
            e.result.now shouldBe err
        }
        on("changing the text to a valid string") {
            e.setText("123")
            it("should compile it") {
                e.result.now shouldEqual Ok(IntLiteral(123))
            }
        }
        on("changing the text to an invalid string") {
            e.setText("invalid")
            it("should not be ok") {
                e.result.now shouldBe err
            }
        }
    }
})
