/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import com.nhaarman.mockitokotlin2.*
import hextant.core.view.TokenEditorView
import hextant.expr.editor.IntLiteralEditor
import hextant.serial.makeRoot
import hextant.test.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.jetbrains.spek.api.dsl.Pending.No
import reaktive.value.now

object TokenEditorSpec : Spek({
    given("a token editor") {
        val context = testingContext()
        val undo: UndoManager = context[UndoManager]
        val editor = IntLiteralEditor(context)
        editor.makeRoot()
        val view = mockView<TokenEditorView>(editor)
        view.inOrder {
            on("adding a view") {
                editor.addView(view)
                it("should display the text") {
                    verify().displayText("")
                }
            }
            on("setting the text") {
                editor.setText("abc")
                it("should notify the view") {
                    verify().displayText("abc")
                }
                test("the undo manager should be able to undo") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing") {
                undo.undo()
                it("should set text back to the empty string") {
                    editor.text.now shouldEqual ""
                }
                test("the view should be notified") {
                    verify().displayText("")
                }
            }
            on("redoing") {
                undo.redo()
                it("should set text to the undone text") {
                    editor.text.now shouldEqual "abc"
                }
                test("the view should be notified") {
                    verify().displayText("abc")
                }
            }
            action("after", No) {
                test("no more interactions") {
                    verify(view, never()).displayText(any())
                }
            }
            Unit
        }
    }
})