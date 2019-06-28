import hextant.core.view.FXListEditorView
import hextant.core.view.FXListEditorView.Orientation.Horizontal
import hextant.expr.edited.Operator
import hextant.expr.editor.*
import hextant.expr.view.*
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
    view<ExprListEditor, FXListEditorView> { editor, args ->
        FXListEditorView.withAltGlyph(editor, FontAwesome.Glyph.PLUS, args, Horizontal).apply {
            cellFactory = { FXListEditorView.SeparatorCell(", ") }
        }
    }
    stylesheet("expr.css")
}