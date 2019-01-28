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
    //Editable expressions
    editable(::EditableOperator, ::EditableOperator)
    editor(::OperatorEditor)
    view(::FXOperatorEditorView)
    editor(::OperatorApplicationEditor)
    view(::FXOperatorApplicationEditorView)
    editor(::SumEditor)
    view(::FXSumEditorView)
    editor(::ExprExpander)
}