package hextant.sample.editor

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import hextant.sample.ast.IntLiteral
import hextant.test.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.on
import reaktive.value.now
import validated.Validated.Invalid
import validated.force
import validated.orNull

internal object IntLiteralEditorSpec : Spek({
    given("an int literal editor") {
        val context = testingContext()
        val editor = IntLiteralEditor(context)
        test("the int literal should initially be an error") {
            editor.result.now shouldBe instanceOf<Invalid>()
        }
        on("setting the text to a valid integer literal") {
            editor.setText("124")
            test("the int literal should be parsed") {
                editor.result.now.force() shouldMatch equalTo(IntLiteral(124))
            }
        }
        on("setting the text to a invalid integer literal") {
            editor.setText("invalid")
            test("the integer literal should equal null") {
                editor.result.now.orNull() shouldMatch absent()
            }
        }
    }
})
