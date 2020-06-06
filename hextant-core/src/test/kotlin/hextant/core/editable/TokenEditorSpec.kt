package hextant.core.editable

import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.serial.makeRoot
import hextant.test.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.value.now
import validated.Validated.Valid
import validated.isValid

object TokenEditorSpec : Spek({
    given("an editable int literal") {
        val context = testingContext()
        val e = IntLiteralEditor(context)
        e.makeRoot()
        test("it should not be ok") {
            e.result.now.isValid shouldBe `false`
        }
        test("e.result should be an Err") {
            e.result.now shouldBe invalid
        }
        on("changing the text to a valid string") {
            e.setText("123")
            it("should compile it") {
                e.result.now shouldEqual Valid(IntLiteral(123))
            }
        }
        on("changing the text to an invalid string") {
            e.setText("invalid")
            it("should not be ok") {
                e.result.now shouldBe invalid
            }
        }
    }
})
