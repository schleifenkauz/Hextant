import hextant.completion.CompletionStrategy
import hextant.core.view.FXExpanderView
import hextant.core.view.ListEditorControl
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.expr.edited.Expr
import hextant.expr.edited.Operator
import hextant.expr.editor.*
import hextant.expr.view.*
import hextant.ok
import hextant.plugin.dsl.plugin
import org.controlsfx.glyphfont.FontAwesome

plugin {
    author = "Nikolaus Knop"
    name = "Hextant Expressions"
    //Int Literals
    defaultEditor(::IntLiteralEditor)
    view(::FXIntLiteralEditorView)
    //Editable expressions
    defaultEditor(::OperatorEditor)
    editor<Operator, OperatorEditor>(::OperatorEditor)
    view(::FXOperatorEditorView)
    defaultEditor(::OperatorApplicationEditor)
    view(::FXOperatorApplicationEditorView)
    defaultEditor(::SumEditor)
    view(::FXSumEditorView)
    defaultEditor(::ExprExpander)
    defaultEditor(::ExprListEditor)
    view { editor: ExprExpander, args ->
        FXExpanderView(editor, args, ExprExpander.config.completer(CompletionStrategy.simple))
    }
    view<ExprListEditor, ListEditorControl> { editor, args ->
        ListEditorControl.withAltGlyph(editor, FontAwesome.Glyph.PLUS, args, Horizontal).apply {
            cellFactory = { ListEditorControl.SeparatorCell(", ") }
        }
    }
    registerConversion<Expr, Int> { expr -> ok(expr.value) }
    stylesheet("expr.css")
}