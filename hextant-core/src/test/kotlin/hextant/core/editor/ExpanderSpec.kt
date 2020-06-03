package hextant.core.editor

import com.nhaarman.mockitokotlin2.inOrder
import hextant.Context
import hextant.core.view.ExpanderView
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.test.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek
import reaktive.value.now
import validated.valid

object ExpanderSpec : Spek({
    GIVEN("an expander") {
        DESCRIBE("compiling result") {
            val context = testingContext()
            val ex = ExpanderSpec.expander(context)
            TEST("editor.now should be null") {
                ex.editor.now shouldBe `null`
            }
            TEST("result.now should be child error") {
                ex.result.now shouldBe invalidComponent
            }
            val editor = IntLiteralEditor(context)
            ON("expanding to an editor with error result") {
                ex.setEditor(editor)
                TEST("result.now should be child error") {
                    ex.result.now shouldBe invalidComponent
                }
            }
            ON("wrapped editor gets ok result") {
                editor.setText("123")
                TEST("result.now should be the result of the wrapped editor") {
                    ex.result.now shouldEqual valid(IntLiteral(123))
                }
            }
            ON("resetting the expander") {
                ex.reset()
                TEST("editor.now should be null") {
                    ex.editor.now shouldBe `null`
                }
                TEST("result.now should be child error") {
                    ex.result.now shouldBe invalidComponent
                }
            }
        }
        DESCRIBE("notifying views") {
            val context = testingContext()
            val ex = ExpanderSpec.expander(context)
            val view = mockView<ExpanderView>()
            ex.addView(view)
            view.inOrder {
                ON("resetting when not expanded") {
                    IT("should thrown an exception") {
                        { ex.reset() }.shouldThrow<IllegalStateException>()
                    }
                }
                ON("expanding invalid text") {
                    ex.expand()
                    IT("should not expand") {
                        verifyNoMoreInteractions()
                    }
                }
                ON("setting text") {
                    ex.setText("123")
                    IT("should notify the views") {
                        verify().displayText("123")
                    }
                }
                ON("expanding") {
                    ex.expand()
                    IT("should set the result") {
                        ex.result.now shouldEqual valid(IntLiteral(123))
                    }
                    IT("should notify the views") {
                        verify().expanded(ex.editor.now!!)
                    }
                }
                ON("expanding again") {
                    IT("should throw an exception") {
                        { ex.expand() }.shouldThrow<IllegalStateException>()
                    }
                }
                ON("reset") {
                    ex.reset()
                    IT("should set the editor to null") {
                        ex.editor.now shouldBe `null`
                    }
                    IT("should set the result to a child error") {
                        ex.result.now shouldBe invalidComponent
                    }
                    IT("should notify the view") {
                        verify().reset()
                    }
                }
            }
        }
        DESCRIBE("undoing and redoing") {
            val context = testingContext()
            val undo = context[UndoManager]
            val ex = ExpanderSpec.expander(context)
            lateinit var content1: IntLiteralEditor
            ON("expanding") {
                ex.setText("123")
                ex.expand()
                content1 = ex.editor.now!!
                IT("should push an edit") {
                    undo.canUndo shouldBe `true`
                }
            }
            ON("undoing") {
                undo.undo()
                IT("should reset") {
                    ex.editor.now shouldBe `null`
                }
            }
            ON("redoing") {
                undo.redo()
                IT("should expand to the last editable") {
                    ex.editor.now shouldEqual content1
                }
            }
            ON("resetting") {
                ex.reset()
                IT("should push an edit") {
                    undo.canUndo shouldBe `true`
                }
            }
            ON("undoing reset") {
                undo.undo()
                IT("should expand to the last editable") {
                    ex.editor.now shouldEqual content1
                }
            }
            ON("redoing reset") {
                undo.redo()
                IT("should reset") {
                    ex.editor.now shouldBe `null`
                }
            }
            ON("setting the text") {
                ex.setText("abc")
                IT("should push an edit") {
                    undo.canUndo shouldBe `true`
                }
            }
            ON("undoing set text") {
                undo.undo()
                IT("should set the text to the previous string") {
                    ex.text.now shouldEqual ""
                }
            }
            ON("redoing set text") {
                undo.redo()
                IT("should set the text to the text set by settext") {
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
