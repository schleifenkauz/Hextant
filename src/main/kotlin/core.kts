import hextant.command.line.CommandLineController
import hextant.command.line.FXCommandLineView
import hextant.core.editor.TextEditor
import hextant.core.expr.editable.EditableIntLiteral
import hextant.core.expr.editable.EditableOperator
import hextant.core.expr.editor.*
import hextant.core.expr.view.*
import hextant.core.list.EditableList
import hextant.core.list.FXListEditorView
import hextant.plugin.dsl.plugin
import hextant.view.FXExpanderView

plugin {
    author = "Nikolaus Knop"
    name = "Hextant"
    //Int Literals
    editable(::EditableIntLiteral, ::EditableIntLiteral)
    editor(::IntLiteralEditor)
    view(::FXIntLiteralEditorView)
    //Command Line
    editor(::CommandLineController)
    view(::FXCommandLineView)
    //Editable expressions
    editable(::EditableOperator, ::EditableOperator)
    editor(::OperatorEditor)
    view(::FXOperatorEditorView)
    editor(::OperatorApplicationEditor)
    view(::FXOperatorApplicationEditorView)
    editor(::SumEditor)
    view(::FXSumEditorView)
    editor(::TextEditor)
    view(::FXTextEditorView)
    //Lists
    view { e: EditableList<*, *>, ctx -> FXListEditorView(e, ctx) }
    //Expanders
    editor(::ExprExpander)
    view(::FXExpanderView)
}