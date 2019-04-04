/**
 *@author Nikolaus Knop
 */

package hextant.core.editable

import com.natpryce.hamkrest.isEmptyString
import com.natpryce.hamkrest.should.shouldMatch
import hextant.defaultNull
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.IntLiteral
import hextant.force
import matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.value.now

object ExpandableSpec : Spek({
    given("an expandable") {
        val e = object : Expandable<IntLiteral, EditableIntLiteral>() {}
        test("the text should be empty") {
            e.text.now shouldMatch isEmptyString
        }
        test("the edited should be null") {
            e.result.now.defaultNull() shouldBe `null`
        }
        test("the editable should be null") {
            e.editable.now shouldBe `null`
        }
        it("should not be expanded") {
            e.isExpanded.now shouldBe `false`
        }
        it("should not be ok") {
            e.isOk shouldBe `false`
        }
        on("setting the text") {
            e.setText("abc")
            it("should set the text to the specified string") {
                e.text.now shouldEqual "abc"
            }
            test("the edited should be null") {
                e.result.now.defaultNull() shouldBe `null`
            }
            test("the editable should be null") {
                e.editable.now shouldBe `null`
            }
            it("should not be expanded") {
                e.isExpanded.now shouldBe `false`
            }
            it("should not be ok") {
                e.isOk shouldBe `false`
            }
        }
        val editable1 = EditableIntLiteral()
        on("setting the content") {
            e.setContent(editable1)
            it("should set the text to the empty string") {
                e.text.now shouldEqual ""
            }
            test("the edited should be null") {
                e.result.now.defaultNull() shouldBe `null`
            }
            test("the editable should be the specified content") {
                e.editable.now shouldEqual editable1
            }
            it("should not be expanded") {
                e.isExpanded.now shouldBe `true`
            }
            it("should not be ok") {
                e.isOk shouldBe `false`
            }
        }
        on("making valid input into the content") {
            editable1.text.set("123")
            it("should set the edited") {
                e.result.now.force() shouldEqual IntLiteral(123)
            }
            it("should be ok") {
                e.isOk shouldBe `true`
            }
        }
        on("making the content invalid") {
            editable1.text.set("not an int")
            it("should set edited to null") {
                e.result.now.defaultNull() shouldBe `null`
            }
            it("should not be ok") {
                e.isOk shouldBe `false`
            }
        }
        on("setting the text") {
            e.setText("a")
            it("should set the text to the specified string") {
                e.text.now shouldEqual "a"
            }
            test("the edited should be null") {
                e.result.now shouldBe `null`
            }
            test("the editable should be null") {
                e.editable.now shouldBe `null`
            }
            it("should not be expanded") {
                e.isExpanded.now shouldBe `false`
            }
            it("should not be ok") {
                e.isOk shouldBe `false`
            }
        }
    }
})