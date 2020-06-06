package hextant.core.editor

import com.nhaarman.mockitokotlin2.inOrder
import hextant.Context
import hextant.core.view.ExpanderView
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.serial.makeRoot
import hextant.test.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.jetbrains.spek.api.dsl.Pending.No
import reaktive.value.now
import validated.valid

object ExpanderSpec : Spek({
    given("an expander") {
        describe("compiling result") {
            val context = testingContext()
            val ex = ExpanderSpec.expander(context)
            ex.makeRoot()
            test("editor.now should be null", No) {
                ex.editor.now shouldBe `null`
            }
            test("result.now should be child error", No) {
                ex.result.now shouldBe invalidComponent
            }
            val editor = IntLiteralEditor(context)
            on("expanding to an editor with error result") {
                ex.setEditor(editor)
                test("result.now should be child error", No) {
                    ex.result.now shouldBe invalidComponent
                }
            }
            on("wrapped editor gets ok result") {
                editor.setText("123")
                test("result.now should be the result of the wrapped editor", No) {
                    ex.result.now shouldEqual valid(IntLiteral(123))
                }
            }
            on("resetting the expander") {
                ex.reset()
                test("editor.now should be null", No) {
                    ex.editor.now shouldBe `null`
                }
                test("result.now should be child error", No) {
                    ex.result.now shouldBe invalidComponent
                }
            }
        }
        describe("notifying views") {
            val context = testingContext()
            val ex = ExpanderSpec.expander(context)
            ex.makeRoot()
            val view = mockView<ExpanderView>()
            ex.addView(view)
            view.inOrder {
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
                    it("should notify the views") {
                        verify().displayText("123")
                    }
                }
                on("expanding") {
                    ex.expand()
                    it("should set the result") {
                        ex.result.now shouldEqual valid(IntLiteral(123))
                    }
                    it("should notify the views") {
                        verify().expanded(ex.editor.now!!)
                    }
                }
                on("expanding again") {
                    it("should throw an exception") {
                        { ex.expand() }.shouldThrow<IllegalStateException>()
                    }
                }
                on("reset") {
                    ex.reset()
                    it("should set the editor to null") {
                        ex.editor.now shouldBe `null`
                    }
                    it("should set the result to a child error") {
                        ex.result.now shouldBe invalidComponent
                    }
                    it("should notify the view") {
                        verify().reset()
                    }
                }
            }
        }
        describe("undoing and redoing") {
            val context = testingContext()
            val undo = context[UndoManager]
            val ex = ExpanderSpec.expander(context)
            ex.makeRoot()
            on("expanding") {
                ex.setText("123")
                ex.expand()
                it("should push an edit") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing") {
                undo.undo()
                it("should reset") {
                    ex.editor.now shouldBe `null`
                }
            }
            on("redoing") {
                undo.redo()
                it("should expand to the last editable") {
                    ex.result.now shouldEqual valid(IntLiteral(123))
                }
            }
            on("resetting") {
                ex.reset()
                it("should push an edit") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing reset") {
                undo.undo()
                it("should expand to the last editable") {
                    ex.result.now shouldEqual valid(IntLiteral(123))
                }
            }
            on("redoing reset") {
                undo.redo()
                it("should reset") {
                    ex.editor.now shouldBe `null`
                }
            }
            on("setting the text") {
                ex.setText("abc")
                it("should push an edit") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing set text") {
                undo.undo()
                it("should set the text to the previous string") {
                    ex.text.now shouldEqual ""
                }
            }
            on("redoing set text") {
                undo.redo()
                it("should set the text to the text set by settext") {
                    ex.text.now shouldEqual "abc"
                }
            }
        }
    }
}) {
    private fun expander(context: Context) = object : Expander<IntLiteral, IntLiteralEditor>(context) {
        override fun expand(text: String): IntLiteralEditor? =
            if (text.toIntOrNull() != null) IntLiteralEditor(context, text) else null
    }
}
