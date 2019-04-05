import hextant.expr.editable.*
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
    editable(::EditableOperatorApplication) { e -> EditableOperatorApplication(platform, e) }
    editor(::OperatorEditor)
    view(::FXOperatorEditorView)
    editor(::OperatorApplicationEditor)
    view(::FXOperatorApplicationEditorView)
    editor(::SumEditor)
    view(::FXSumEditorView)
    editor(::ExprExpander)
    editor(::ExprListEditor)
    expandable(::ExpandableExpr)
}