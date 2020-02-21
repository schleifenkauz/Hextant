/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import com.nhaarman.mockitokotlin2.*
import hextant.core.view.TokenEditorView
import hextant.expr.editor.IntLiteralEditor
import hextant.test.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek
import reaktive.value.now

object TokenEditorSpec : Spek({
    GIVEN("a token editor") {
        val context = testingContext()
        val undo: UndoManager = context[UndoManager]
        val editor = IntLiteralEditor(context)
        val view = mockView<TokenEditorView>()
        view.inOrder {
            ON("adding a view") {
                editor.addView(view)
                IT("should display the text") {
                    verify().displayText("")
                }
            }
            ON("setting the text") {
                editor.setText("abc")
                IT("should notify the view") {
                    verify().displayText("abc")
                }
                test("the undo manager should be able to undo") {
                    undo.canUndo shouldBe `true`
                }
            }
            ON("undoing") {
                undo.undo()
                IT("should set text back to the empty string") {
                    editor.text.now shouldEqual ""
                }
                test("the view should be notified") {
                    verify().displayText("")
                }
            }
            ON("redoing") {
                undo.redo()
                IT("should set text to the undone text") {
                    editor.text.now shouldEqual "abc"
                }
                test("the view should be notified") {
                    verify().displayText("abc")
                }
            }
            ACTION("after") {
                test("no more interactions") {
                    verify(view, never()).displayText(any())
                }
            }
        }
    }
})