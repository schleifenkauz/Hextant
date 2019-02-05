/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import com.nhaarman.mockitokotlin2.*
import hextant.Context.Companion.newInstance
import hextant.bundle.CorePermissions.Public
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.expr.view.IntLiteralEditorView
import hextant.getEditor
import hextant.undo.UndoManager
import hextant.undo.UndoManagerImpl
import matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.value.now

object TokenEditorSpec : Spek({
    given("a token editor") {
        val undo: UndoManager = UndoManagerImpl()
        val context = newInstance {
            set(Public, UndoManager, undo)
        }
        val editable = EditableIntLiteral()
        val editor = context.getEditor(editable) as IntLiteralEditor
        val view = mock<IntLiteralEditorView> {
            on { onGuiThread(any()) }.then { it.getArgument<() -> Unit>(0).invoke() }
        }
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
                    editable.text.now shouldEqual ""
                }
                test("the view should be notified") {
                    verify().displayText("")
                }
            }
            on("redoing") {
                undo.redo()
                it("should set text to the undone text") {
                    editable.text.now shouldEqual "abc"
                }
                test("the view should be notified") {
                    verify().displayText("abc")
                }
            }
            action("after") {
                test("no more interactions") {
                    verify(view, never()).displayText(any())
                }
            }
        }
    }
})