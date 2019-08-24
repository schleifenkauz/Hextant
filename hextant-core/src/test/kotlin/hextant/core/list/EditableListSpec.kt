///**
// *@author Nikolaus Knop
// */
//
package hextant.core.list
//
//import hextant.*
//import hextant.expr.editor.IntLiteralEditor
//import hextant.expr.edited.IntLiteral
//import hextant.test.matchers.*
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.given
//import org.jetbrains.spek.api.dsl.on
//
//object EditableListSpec : Spek({
//    given("an editable list e") {
//        val e = EditableList<IntLiteral, IntLiteralEditor>()
//        test("e.result should be empty now") {
//            val edited = e.result.now
//            edited.force() shouldMatch isEmpty
//        }
//        test("the editable list should be empty now") {
//            e.editableList.now shouldMatch isEmpty
//        }
//        test("the result list should be empty now") {
//            e.resultList.now shouldMatch isEmpty
//        }
//        val editable1 = IntLiteralEditor(1)
//        on("adding an element to the editable list") {
//            e.editableList.now.add(editable1)
//            test("the result list contains the result of the added editable") {
//                e.resultList.now shouldMatch contains<CompileResult<*>>(Ok(IntLiteral(1)))
//            }
//            test("e.result contains the result of the added editable") {
//                e.result.now.force() shouldMatch contains(IntLiteral(1))
//            }
//        }
//        on("modifying an int literal to be invalid") {
//            editable1.text.set("not an int")
//            test("the result list contains an Err") {
//                e.resultList.now.size shouldEqual 1
//                e.resultList.now[0] shouldBe hextant.test.matchers.err
//            }
//            test("e.result should be a child Err") {
//                e.result.now shouldBe hextant.test.matchers.childErr
//            }
//        }
//        on("making it valid again") {
//            editable1.text.set("123")
//            test("the result list contains the compiled literal") {
//                e.resultList.now shouldEqual listOf(Ok(IntLiteral(123)))
//            }
//            test("e.result contains the compiled literal") {
//                e.result.now.force() shouldEqual listOf(IntLiteral(123))
//            }
//        }
//        val editable2 = IntLiteralEditor(2)
//        on("adding yet another editable int literal") {
//            e.editableList.now.add(editable2)
//            test("the result list contains the compiled literal") {
//                e.resultList.now shouldMatch contains<CompileResult<*>>(Ok(IntLiteral(2)))
//            }
//            test("e.result contains the compiled literal") {
//                e.result.now.force() shouldMatch contains(IntLiteral(2))
//            }
//        }
//    }
//})