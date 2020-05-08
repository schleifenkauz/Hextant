package hextant.core.list

import com.nhaarman.mockitokotlin2.*
import hextant.core.editor.ListEditor
import hextant.core.view.ListEditorView
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.test.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek

object ListEditorSpec : Spek({
    GIVEN("a ListEditor") {
        DESCRIBE("adding, removing and clearing") {
            val ctx = testingContext()
            val editor = object : ListEditor<IntLiteral, IntLiteralEditor>(ctx) {
                override fun createEditor(): IntLiteralEditor = IntLiteralEditor(context)
            }
            val view = mockView<ListEditorView>()
            view.inOrder {
                ON("adding a view") {
                    editor.addView(view)
                    IT("should notify the view to be empty") {
                        verify().empty()
                    }
                }
                ON("adding a new element") {
                    editor.addAt(0)
                    IT("should create a new editable and add it") {
                        editor.results.now.size shouldEqual 1
                    }
                    IT("should notify the views") {
                        verify().notEmpty()
                        verify().added(any(), eq(0))
                    }
                }
                ON("adding another element") {
                    editor.addAt(1)
                    IT("should create a new editable and add it") {
                        editor.results.now.size shouldEqual 2
                    }
                    IT("should notify the views") {
                        verify().added(any(), eq(1))
                    }
                }
                ON("removing an element") {
                    editor.removeAt(0)
                    IT("should remove an element from the editable list") {
                        editor.results.now.size shouldEqual 1
                    }
                    IT("should notify the views") {
                        verify().removed(0)
                    }
                }
                ON("removing the last element") {
                    editor.removeAt(0)
                    TEST("the results list should be empty") {
                        editor.results.now.size shouldEqual 0
                    }
                    IT("should notify the views about being empty") {
                        verify().removed(0)
                        verify().empty()
                    }
                }
            }
        }
        DESCRIBE("undo/redo") {
            val ctx = testingContext()
            val undo = ctx[UndoManager]
            val editor = object :
                ListEditor<IntLiteral, IntLiteralEditor>(ctx) {
                override fun createEditor(): IntLiteralEditor = IntLiteralEditor(context)
            }
            val view = mockView<ListEditorView>()
            editor.addView(view)
            ON("adding an editable") {
                editor.addAt(0)
                IT("should be able to undo") {
                    undo.canUndo shouldBe `true`
                }
            }
            ON("undoing") {
                undo.undo()
                IT("should remove the editable") {
                    editor.results.now.size shouldEqual 0
                }
            }
            ON("redoing") {
                undo.redo()
                IT("should add the editable again") {
                    editor.results.now.size shouldEqual 1
                }
            }
            ON("removing an element") {
                editor.removeAt(0)
                IT("should be able to undo") {
                    undo.canUndo shouldBe `true`
                }
            }
            ON("undoing removing the element") {
                undo.undo()
                IT("should add the element again") {
                    editor.results.now.size shouldEqual 1
                }
            }
            ON("redoing removing the element") {
                undo.redo()
                IT("should remove the element again") {
                    editor.results.now.size shouldEqual 0
                }
            }
        }
    }
})