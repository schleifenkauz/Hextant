import hextant.expr.editor.*
import hextant.expr.view.*
import hextant.plugin.dsl.plugin

plugin {
    author = "Nikolaus Knop"
    name = "Hextant Expressions"
    //Int Literals
    defaultEditor(::IntLiteralEditor)
    view(::FXIntLiteralEditorView)
    //Editable expressions
    defaultEditor(::OperatorEditor)
    editor(::OperatorEditor)
    view(::FXOperatorEditorView)
    defaultEditor(::OperatorApplicationEditor)
    view(::FXOperatorApplicationEditorView)
    defaultEditor(::SumEditor)
    view(::FXSumEditorView)
    defaultEditor(::ExprExpander)
    defaultEditor(::ExprListEditor)
}