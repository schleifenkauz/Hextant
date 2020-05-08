package hextant.core.editable

import hextant.Ok
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.isOk
import hextant.test.*
import org.jetbrains.spek.api.Spek
import reaktive.value.now

object TokenEditorSpec : Spek({
    GIVEN("an editable int literal") {
        val context = testingContext()
        val e = IntLiteralEditor(context)
        test("it should not be ok") {
            e.result.now.isOk shouldBe `false`
        }
        test("e.result should be an Err") {
            e.result.now shouldBe err
        }
        ON("changing the text to a valid string") {
            e.setText("123")
            IT("should compile it") {
                e.result.now shouldEqual Ok(IntLiteral(123))
            }
        }
        ON("changing the text to an invalid string") {
            e.setText("invalid")
            IT("should not be ok") {
                e.result.now shouldBe err
            }
        }
    }
})
