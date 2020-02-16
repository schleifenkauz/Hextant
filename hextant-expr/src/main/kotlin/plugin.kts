import hextant.completion.CompletionStrategy
import hextant.core.view.FXExpanderView
import hextant.core.view.ListEditorControl
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.expr.edited.Expr
import hextant.expr.edited.Operator
import hextant.expr.editor.*
import hextant.expr.view.FXOperatorApplicationEditorView
import hextant.ok
import hextant.plugin.dsl.plugin
import org.controlsfx.glyphfont.FontAwesome

plugin {
    author = "Nikolaus Knop"
    name = "Hextant Expressions"
    defaultEditor(::IntLiteralEditor)
    defaultEditor(::OperatorEditor)
    editor<Operator, OperatorEditor>(::OperatorEditor)
    defaultEditor(::OperatorApplicationEditor)
    view(::FXOperatorApplicationEditorView)
    defaultEditor(::SumEditor)
    defaultEditor(::ExprExpander)
    defaultEditor(::ExprListEditor)
    view { editor: ExprExpander, args ->
        FXExpanderView(editor, args, ExprExpander.config.completer(CompletionStrategy.simple))
    }
    tokenEditorView<OperatorEditor>("operator")
    tokenEditorView<IntLiteralEditor>("decimal-editor")
    compoundView { e: SumEditor ->
        keyword("sum")
        space()
        view(e.expressions)
    }
    view<ExprListEditor, ListEditorControl> { editor, args ->
        ListEditorControl.withAltGlyph(editor, FontAwesome.Glyph.PLUS, args, Horizontal).apply {
            cellFactory = { ListEditorControl.SeparatorCell(", ") }
        }
    }
    registerConversion<Expr, Int> { expr -> ok(expr.value) }
    stylesheet("expr.css")
}