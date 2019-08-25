package hextant.core.list
//
//import com.nhaarman.mockitokotlin2.*
//import hextant.*
//import hextant.bundle.CorePermissions.Public
//import hextant.expr.edited.IntLiteral
//import hextant.mocking.viewMock
//import hextant.test.matchers.*
//import hextant.undo.UndoManager
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.*
//
//object ListEditorSpec : Spek({
//    GIVEN("a ListEditor") {
//        DESCRIBE("addding, removing and clearing") {
//            val ctx = Context.newInstance(HextantPlatform.singleThread()) {
//                set(Public, UndoManager, UndoManager.newInstance())
//            }
//            val editable = EditableList<IntLiteral, hextant.expr.editor.IntLiteralEditor>()
//            val editor = object : ListEditor<hextant.expr.editor.IntLiteralEditor, EditableList<*, hextant.expr.editor.IntLiteralEditor>>(editable, ctx) {
//                override fun createNewEditable(): hextant.expr.editor.IntLiteralEditor = hextant.expr.editor.IntLiteralEditor()
//
//                override fun accepts(child: Editor<*>): Boolean = child is IntLiteralEditor
//            }
//            val view = viewMock<ListEditorView>()
//            view.inOrder {
//                ON("adding a view") {
//                    editor.addView(view)
//                    IT("should notify the view to be empty") {
//                        verify().empty()
//                    }
//                }
//                ON("adding a new element") {
//                    editor.add(0)
//                    IT("should create a new editable and add it") {
//                        editable.editableList.now.size shouldEqual 1
//                    }
//                    IT("should notify the views") {
//                        verify().added(any(), eq(0))
//                        verify().notEmpty()
//                    }
//                }
//                ON("adding another element") {
//                    editor.add(1)
//                    IT("should create a new editable and add it") {
//                        editable.editableList.now.size shouldEqual 2
//                    }
//                    IT("should notify the views") {
//                        verify().added(any(), eq(1))
//                    }
//                }
//                ON("removing an element") {
//                    editor.removeAt(0)
//                    IT("should remove an element from the editable list") {
//                        editable.editableList.now.size shouldEqual 1
//                    }
//                    IT("should notify the views") {
//                        verify().removed(0)
//                    }
//                }
//                ON("clearing") {
//                    editor.clear()
//                    IT("should remove all elements from the editable list") {
//                        editable.editableList.now.size shouldEqual 0
//                    }
//                    IT("should notify the views") {
//                        verify().removed(0)
//                        verify().empty()
//                    }
//                }
//            }
//            afterGroup { ctx.platform.exit() }
//        }
//        DESCRIBE("undo/redo") {
//            val undo = UndoManager.newInstance()
//            val ctx = Context.newInstance(HextantPlatform.singleThread()) {
//                set(Public, UndoManager, undo)
//            }
//            val editable = EditableList<IntLiteral, hextant.expr.editor.IntLiteralEditor>()
//            val editor = object : ListEditor<hextant.expr.editor.IntLiteralEditor, EditableList<*, hextant.expr.editor.IntLiteralEditor>>(editable, ctx) {
//                override fun createNewEditable(): hextant.expr.editor.IntLiteralEditor = hextant.expr.editor.IntLiteralEditor()
//
//                override fun accepts(child: Editor<*>): Boolean = child is IntLiteralEditor
//            }
//            val view = viewMock<ListEditorView>()
//            editor.addView(view)
//            ON("adding an editable") {
//                editor.add(0)
//                IT("should be able to undo") {
//                    undo.canUndo shouldBe `true`
//                }
//            }
//            ON("undoing") {
//                undo.undo()
//                IT("should remove the editable") {
//                    editable.editableList.now.size shouldEqual 0
//                }
//            }
//            ON("redoing") {
//                undo.redo()
//                IT("should add the editable again") {
//                    editable.editableList.now.size shouldEqual 1
//                }
//            }
//            ON("removing an element") {
//                editor.removeAt(0)
//                IT("should be able to undo") {
//                    undo.canUndo shouldBe `true`
//                }
//            }
//            ON("undoing removing the element") {
//                undo.undo()
//                IT("should add the element again") {
//                    editable.editableList.now.size shouldEqual 1
//                }
//            }
//            ON("redoing removing the element") {
//                undo.redo()
//                IT("should remove the element again") {
//                    editable.editableList.now.size shouldEqual 0
//                }
//            }
//            afterGroup { ctx.platform.exit() }
//        }
//    }
//})