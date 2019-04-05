package hextant.core.editor

import com.nhaarman.mockitokotlin2.inOrder
import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.completion.NoCompleter
import hextant.core.editable.Expandable
import hextant.core.editor.ExpanderSpec.expandable
import hextant.core.editor.ExpanderSpec.expander
import hextant.core.view.ExpanderView
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.mocking.viewMock
import hextant.test.matchers.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.value.now

object ExpanderSpec : Spek({
    given("an expander") {
        describe("expanding and resetting") {
            val context = Context.newInstance(HextantPlatform.singleThread()) {
                set(Public, UndoManager, UndoManager.newInstance())
            }
            val e = expandable()
            val ex = expander(e, context)
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
                        e.editable.now!!.result.now.force().value shouldEqual 123
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
                on("reset") {
                    ex.reset()
                    it("should reset the expandable") {
                        e.editable.now shouldBe `null`
                    }
                    it("should notify the view") {
                        verify().reset()
                    }
                }
                afterGroup { context.platform.exit() }
            }
        }
        describe("undoing and redoing") {
            val undo: UndoManager = UndoManager.newInstance()
            val context = Context.newInstance(HextantPlatform.singleThread()) {
                set(Public, UndoManager, undo)
            }
            val e = expandable()
            val ex = expander(e, context)
            lateinit var content1: EditableIntLiteral
            on("expanding") {
                ex.setText("123")
                ex.expand()
                content1 = e.editable.now!!
                it("should push an edit") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing") {
                undo.undo()
                it("should reset") {
                    e.editable.now shouldBe `null`
                }
            }
            on("redoing") {
                undo.redo()
                it("should expand to the last editable") {
                    e.editable.now shouldEqual content1
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
                    e.editable.now shouldEqual content1
                }
            }
            on("redoing reset") {
                undo.redo()
                it("should reset") {
                    e.editable.now shouldBe `null`
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
                    e.text.now shouldEqual ""
                }
            }
            on("redoing set text") {
                undo.redo()
                it("should set the text to the text set by setText") {
                    e.text.now shouldEqual "abc"
                }
            }
            afterGroup { context.platform.exit() }
        }
    }
}) {
    fun expandable() = object : Expandable<IntLiteral, EditableIntLiteral>() {}

    fun expander(expandable: Expandable<IntLiteral, EditableIntLiteral>, context: Context) =
        object :
            Expander<EditableIntLiteral, Expandable<IntLiteral, EditableIntLiteral>>(expandable, context, NoCompleter) {
            override fun expand(text: String): EditableIntLiteral? {
                val int = text.toIntOrNull() ?: return null
                return EditableIntLiteral(int)
            }

            override fun accepts(child: Editor<*>): Boolean = child is IntLiteralEditor
        }
}