package hextant.core.editor

import com.nhaarman.mockitokotlin2.inOrder
import hextant.Context
import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.core.editable.Expandable
import hextant.core.view.ExpanderView
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.mocking.viewMock
import hextant.undo.UndoManager
import hextant.undo.UndoManagerImpl
import matchers.shouldEqual
import matchers.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.value.now

object ExpanderSpec : Spek({
    given("an expander") {
        val undo: UndoManager = UndoManagerImpl()
        val context = Context.newInstance {
            set(Public, UndoManager, undo)
        }
        val e = object : Expandable<IntLiteral, EditableIntLiteral>() {}
        val ex = object : Expander<EditableIntLiteral, Expandable<IntLiteral, EditableIntLiteral>>(e, context) {
            override fun expand(text: String): EditableIntLiteral? {
                val int = text.toIntOrNull() ?: return null
                return EditableIntLiteral(int)
            }

            override fun accepts(child: Editor<*>): Boolean = child is IntLiteralEditor
        }
        val view = viewMock<ExpanderView>()
        view.inOrder {
            on("adding a view") {
                ex.addView(view)
                test("the view should be reset") {
                    verify().reset()
                }
            }
            on("resetting when not expanded") {
                it("should thrown an exception") {
                    { ex.reset() }.shouldThrow<IllegalStateException>()
                }
            }
            on("expanding invalid text") {
                ex.expand()
                it("should not expand") {
                    verifyNoMoreInteractions()
                }
            }
            on("setting text") {
                ex.setText("123")
                it("should set the text") {
                    e.text.now shouldEqual "123"
                }
                it("should notify the views") {
                    verify().textChanged("123")
                }
            }
            on("expanding") {
                ex.expand()
                it("should set the content of the expandable") {
                    e.editable.now!!.edited.now!!.value shouldEqual 123
                }
                it("should notify the views") {
                    verify().expanded(e.editable.now!!)
                }
            }
            on("expanding again") {
                it("should throw an exception") {
                    { ex.expand() }.shouldThrow<IllegalStateException>()
                }
            }
            describe("undo and redo") {

            }
        }
    }
})
