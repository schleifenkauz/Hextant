/**
 *@author Nikolaus Knop
 */

package hextant.core.list

import com.natpryce.hamkrest.should.shouldMatch
import hextant.*
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.IntLiteral
import matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.on
import reaktive.value.now

object EditableListSpec : Spek({
    given("an editable list e") {
        val e = EditableList<IntLiteral, EditableIntLiteral>()
        test("e.result should be empty now") {
            val edited = e.result.now
            edited shouldMatch isOk(isEmpty)
        }
        test("the editable list should be empty now") {
            e.editableList.now shouldMatch isEmpty
        }
        test("the edited list should be empty now") {
            e.editedList.now shouldMatch isEmpty
        }
        val editable1 = EditableIntLiteral(1)
        on("adding an element to the editable list") {
            e.editableList.now.add(editable1)
            test("the edited list contains the edited of the added editable") {
                e.editedList.now shouldMatch contains<CompileResult<*>>(Ok(IntLiteral(1)))
            }
            test("e.edited contains the edited of the added editable") {
                e.result.now shouldMatch isOk(contains(IntLiteral(1)))
            }
        }
        on("modifying an int literal to be invalid") {
            editable1.text.set("not an int")
            test("the edited list contains an Err") {
                e.editedList.now.size shouldEqual 1
                e.editedList.now[0] shouldBe err
            }
            test("e.result should be a child Err") {
                e.result.now shouldBe childErr
            }
        }
        on("making it valid again") {
            editable1.text.set("123")
            test("the edited list contains the compiled literal") {
                e.editedList.now shouldEqual listOf(Ok(IntLiteral(123)))
            }
            test("e.edited list contains the compiled literal") {
                e.result.now.force() shouldEqual listOf(IntLiteral(123))
            }
        }
        val editable2 = EditableIntLiteral(2)
        on("adding yet another editable int literal") {
            e.editableList.now.add(editable2)
            test("the edited list contains the compiled literal") {
                e.editedList.now shouldMatch contains<CompileResult<*>>(Ok(IntLiteral(2)))
            }
            test("e.edited contains the compiled literal") {
                e.result.now.force() shouldMatch contains(IntLiteral(2))
            }
        }
    }
})