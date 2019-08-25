///**
// *@author Nikolaus Knop
// */
//
package hextant.core.editable
//
//import com.natpryce.hamkrest.isEmptyString
//import hextant.Ok
//import hextant.expr.editor.IntLiteralEditor
//import hextant.expr.edited.IntLiteral
//import hextant.test.matchers.*
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.*
//
//object ExpandableSpec : Spek({
//    GIVEN("an expandable") {
//        val e = object : Expandable<IntLiteral, IntLiteralEditor>() {}
//        test("the text should be empty") {
//            e.text.now shouldMatch isEmptyString
//        }
//        test("the result should be a childErr") {
//            e.result.now shouldBe childErr
//        }
//        test("the editable should be null") {
//            e.editable.now shouldBe `null`
//        }
//        IT("should not be expanded") {
//            e.isExpanded.now shouldBe `false`
//        }
//        IT("should not be ok") {
//            e.isOk shouldBe `false`
//        }
//        ON("setting the text") {
//            e.setText("abc")
//            IT("should set the text to the specified string") {
//                e.text.now shouldEqual "abc"
//            }
//            test("the result should be an ChildErr") {
//                e.result.now shouldBe childErr
//            }
//            test("the editable should be null") {
//                e.editable.now shouldBe `null`
//            }
//            IT("should not be expanded") {
//                e.isExpanded.now shouldBe `false`
//            }
//            IT("should not be ok") {
//                e.isOk shouldBe `false`
//            }
//        }
//        val editable1 = IntLiteralEditor()
//        ON("setting the content") {
//            e.setContent(editable1)
//            IT("should set the text to the empty string") {
//                e.text.now shouldEqual ""
//            }
//            test("the result should be a child error") {
//                e.result.now shouldBe childErr
//            }
//            test("the editable should be the specified content") {
//                e.editable.now shouldEqual editable1
//            }
//            IT("should not be expanded") {
//                e.isExpanded.now shouldBe `true`
//            }
//            IT("should not be ok") {
//                e.isOk shouldBe `false`
//            }
//        }
//        ON("making valid input into the content") {
//            editable1.text.set("123")
//            IT("should set the result") {
//                e.result.now shouldEqual Ok(IntLiteral(123))
//            }
//            IT("should be ok") {
//                e.isOk shouldBe `true`
//            }
//        }
//        ON("making the content invalid") {
//            editable1.text.set("not an int")
//            IT("should set result to a child error") {
//                e.result.now shouldBe childErr
//            }
//            IT("should not be ok") {
//                e.isOk shouldBe `false`
//            }
//        }
//        ON("setting the text") {
//            e.setText("a")
//            IT("should set the text to the specified string") {
//                e.text.now shouldEqual "a"
//            }
//            test("the result should be a child error") {
//                e.result.now shouldBe childErr
//            }
//            test("the editable should be null") {
//                e.editable.now shouldBe `null`
//            }
//            IT("should not be expanded") {
//                e.isExpanded.now shouldBe `false`
//            }
//            IT("should not be ok") {
//                e.isOk shouldBe `false`
//            }
//        }
//    }
//})