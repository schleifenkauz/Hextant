import hextant.blocky.editor.*
import hextant.blocky.view.*
import hextant.core.view.EditorControlWrapper
import hextant.core.view.FXTokenEditorView
import hextant.createView
import hextant.plugin.dsl.plugin

plugin {
    name = "Blocky"
    author = "Nikolaus Knop"
    view { e: IdEditor, args ->
        FXTokenEditorView(e, args).apply {
            root.styleClass.add("id")
        }
    }
    view { e: RefEditor, args ->
        val v = e.context.createView(e.id) as FXTokenEditorView
        EditorControlWrapper(e, v, args)
    }
    compoundView<BinaryExpressionEditor> { e ->
        line {
            spacing = 2.0
            view(e.left)
            view(e.op)
            view(e.right)
        }
    }
    compoundView<UnaryExpressionEditor> { e ->
        line {
            spacing = 2.0
            view(e.op)
            view(e.operand)
        }
    }
    compoundView<AssignEditor> { e ->
        line {
            spacing = 2.0
            view(e.name)
            operator("<-")
            view(e.value)
        }
    }
    compoundView<PrintEditor> { e ->
        line {
            spacing = 2.0
            keyword("print")
            view(e.expr)
        }
    }
    compoundView<SwapEditor> { e ->
        line {
            spacing = 2.0
            view(e.left)
            operator("<->")
            view(e.right)
        }
    }
    view { e: IntLiteralEditor, args ->
        FXTokenEditorView(e, args).apply {
            root.styleClass.add("int-literal")
        }
    }
    view { e: UnaryOperatorEditor, args ->
        FXTokenEditorView(e, args).apply {
            root.styleClass.add("operator")
        }
    }
    view { e: BinaryOperatorEditor, args ->
        FXTokenEditorView(e, args).apply {
            root.styleClass.add("operator")
        }
    }
    view(::EntryEditorControl)
    view(::BlockEditorControl)
    view(::BranchEditorControl)
    view(::ProgramEditorControl)
    stylesheet("hextant/blocky/style.css")
}