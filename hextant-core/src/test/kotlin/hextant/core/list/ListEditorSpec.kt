package hextant.core.list

import com.nhaarman.mockitokotlin2.*
import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.edited.IntLiteral
import hextant.expr.editor.IntLiteralEditor
import hextant.mocking.viewMock
import hextant.test.matchers.*
import hextant.undo.UndoManager
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

object ListEditorSpec : Spek({
    given("a list editor") {
        describe("addding, removing and clearing") {
            val ctx = Context.newInstance(HextantPlatform.singleThread()) {
                set(Public, UndoManager, UndoManager.newInstance())
            }
            val editable = EditableList<IntLiteral, EditableIntLiteral>()
            val editor = object : ListEditor<EditableIntLiteral, EditableList<*, EditableIntLiteral>>(editable, ctx) {
                override fun createNewEditable(): EditableIntLiteral = EditableIntLiteral()

                override fun accepts(child: Editor<*>): Boolean = child is IntLiteralEditor
            }
            val view = viewMock<ListEditorView>()
            view.inOrder {
                on("adding a view") {
                    editor.addView(view)
                    it("should notify the view to be empty") {
                        verify().empty()
                    }
                }
                on("adding a new element") {
                    editor.add(0)
                    it("should create a new editable and add it") {
                        editable.editableList.now.size shouldEqual 1
                    }
                    it("should notify the views") {
                        verify().added(any(), eq(0))
                        verify().notEmpty()
                    }
                }
                on("adding another element") {
                    editor.add(1)
                    it("should create a new editable and add it") {
                        editable.editableList.now.size shouldEqual 2
                    }
                    it("should notify the views") {
                        verify().added(any(), eq(1))
                    }
                }
                on("removing an element") {
                    editor.removeAt(0)
                    it("should remove an element from the editable list") {
                        editable.editableList.now.size shouldEqual 1
                    }
                    it("should notify the views") {
                        verify().removed(0)
                    }
                }
                on("clearing") {
                    editor.clear()
                    it("should remove all elements from the editable list") {
                        editable.editableList.now.size shouldEqual 0
                    }
                    it("should notify the views") {
                        verify().removed(0)
                        verify().empty()
                    }
                }
            }
            afterGroup { ctx.platform.exit() }
        }
        describe("undo/redo") {
            val undo = UndoManager.newInstance()
            val ctx = Context.newInstance(HextantPlatform.singleThread()) {
                set(Public, UndoManager, undo)
            }
            val editable = EditableList<IntLiteral, EditableIntLiteral>()
            val editor = object : ListEditor<EditableIntLiteral, EditableList<*, EditableIntLiteral>>(editable, ctx) {
                override fun createNewEditable(): EditableIntLiteral = EditableIntLiteral()

                override fun accepts(child: Editor<*>): Boolean = child is IntLiteralEditor
            }
            val view = viewMock<ListEditorView>()
            editor.addView(view)
            on("adding an editable") {
                editor.add(0)
                it("should be able to undo") {
                    undo.canUndo shouldBe `true`
                }
            }
            on("undoing") {
                undo.undo()
                it("should remove the editable") {
                    editable.editableList.now.size shouldEqual 0
                }
            }
            on("redoing") {
                undo.redo()
                it("should add the editable again") {
                    editable.editableList.now.size shouldEqual 1
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
                    editable.editableList.now.size shouldEqual 1
                }
            }
            on("redoing removing the element") {
                undo.redo()
                it("should remove the element again") {
                    editable.editableList.now.size shouldEqual 0
                }
            }
            afterGroup { ctx.platform.exit() }
        }
    }
})