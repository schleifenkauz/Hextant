import hextant.command.line.CommandLineController
import hextant.command.line.FXCommandLineView
import hextant.core.editor.TextEditor
import hextant.core.list.EditableList
import hextant.core.list.FXListEditorView
import hextant.core.view.FXExpanderView
import hextant.expr.editable.EditableIntLiteral
import hextant.expr.editable.EditableOperator
import hextant.expr.editor.*
import hextant.expr.view.*
import hextant.plugin.dsl.plugin

plugin {
    author = "Nikolaus Knop"
    name = "Hextant Expressions"
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