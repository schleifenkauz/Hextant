///**
// *@author Nikolaus Knop
// */
//
package hextant.core.editor
//
//import com.nhaarman.mockitokotlin2.*
//import hextant.Context.Companion.newInstance
//import hextant.HextantPlatform
//import hextant.bundle.CorePermissions.Public
//import hextant.expr.view.IntLiteralEditorView
//import hextant.getEditor
//import hextant.mocking.viewMock
//import hextant.test.matchers.*
//import hextant.undo.UndoManager
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.*
//
//object TokenEditorSpec : Spek({
//    GIVEN("a token editor") {
//        val undo: UndoManager = UndoManager.newInstance()
//        val context = newInstance(HextantPlatform.singleThread()) {
//            set(Public, UndoManager, undo)
//        }
//        val editable = hextant.expr.editor.IntLiteralEditor()
//        val editor = context.getEditor(editable) as IntLiteralEditor
//        val view = viewMock<IntLiteralEditorView>()
//        view.inOrder {
//            ON("adding a view") {
//                editor.addView(view)
//                IT("should display the text") {
//                    verify().displayText("")
//                }
//            }
//            ON("setting the text") {
//                editor.setText("abc")
//                IT("should notify the view") {
//                    verify().displayText("abc")
//                }
//                test("the undo manager should be able to undo") {
//                    undo.canUndo shouldBe `true`
//                }
//            }
//            ON("undoing") {
//                undo.undo()
//                IT("should set text back to the empty string") {
//                    editable.text.now shouldEqual ""
//                }
//                test("the view should be notified") {
//                    verify().displayText("")
//                }
//            }
//            ON("redoing") {
//                undo.redo()
//                IT("should set text to the undone text") {
//                    editable.text.now shouldEqual "abc"
//                }
//                test("the view should be notified") {
//                    verify().displayText("abc")
//                }
//            }
//            ACTION("after") {
//                test("no more interactions") {
//                    verify(view, never()).displayText(any())
//                }
//            }
//            afterGroup { context.platform.exit() }
//        }
//    }
//})