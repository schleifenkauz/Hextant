package hextant.core.editor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import hextant.core.view.ListEditorView
import hextant.expr.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.serial.makeRoot
import hextant.test.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.Pending.No
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object ListEditorSpec : Spek({
    given("a ListEditor") {
        describe("adding, removing and clearing") {
            val ctx = testingContext()
            val editor = object : ListEditor<IntLiteral?, IntLiteralEditor>(ctx) {
                override fun createEditor(): IntLiteralEditor = IntLiteralEditor(context)
            }
            editor.makeRoot()
            val view = mockView<ListEditorView>(editor)
            view.inOrder {
                on("adding a view") {
                    editor.addView(view)
                    it("should notify the view to be empty") {
                        verify().empty()
                    }
                }
                on("adding a new element") {
                    editor.addAt(0)
                    it("should create a new editable and add it") {
                        editor.results.now.size shouldEqual 1
                    }
                    it("should notify the views") {
                        verify().notEmpty()
                        verify().added(any(), eq(0))
                    }
                }
                on("adding another element") {
                    editor.addAt(1)
                    it("should create a new editable and add it") {
                        editor.results.now.size shouldEqual 2
                    }
                    it("should notify the views") {
                        verify().added(any(), eq(1))
                    }
                }
                on("removing an element") {
                    editor.removeAt(0)
                    it("should remove an element from the editable list") {
                        editor.results.now.size shouldEqual 1
                    }
                    it("should notify the views") {
                        verify().removed(0)
                    }
                }
                on("removing the last element") {
                    editor.removeAt(0)
                    test("the results list should be empty", No) {
                        editor.results.now.size shouldEqual 0
                    }
                    it("should notify the views about being empty") {
                        verify().removed(0)
                        verify().empty()
                    }
                }
            }
        }
        describe("undo/redo") {
            val ctx = testingContext()
            val undo = ctx[UndoManager]
            val editor = object : ListEditor<IntLiteral?, IntLiteralEditor>(ctx) {
                override fun createEditor(): IntLiteralEditor = IntLiteralEditor(context)
            }
            editor.makeRoot()
            val view = mockView<ListEditorView>(editor)
            editor.addView(view)
            on("adding an editable") {
                editor.addAt(0)
                it("should be able to undo") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing") {
                undo.undo()
                it("should remove the editable") {
                    editor.results.now.size shouldEqual 0
                }
            }
            on("redoing") {
                undo.redo()
                it("should add the editable again") {
                    editor.results.now.size shouldEqual 1
                }
            }
            on("removing an element") {
                editor.removeAt(0)
                it("should be able to undo") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing removing the element") {
                undo.undo()
                it("should add the element again") {
                    editor.results.now.size shouldEqual 1
                }
            }
            on("redoing removing the element") {
                undo.redo()
                it("should remove the element again") {
                    editor.results.now.size shouldEqual 0
                }
            }
        }
    }
})