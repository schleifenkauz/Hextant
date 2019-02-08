import hextant.core.list.FXListEditorView
import hextant.getEditable
import hextant.lisp.editable.*
import hextant.lisp.editor.*
import hextant.lisp.view.*
import hextant.plugin.dsl.plugin
import org.controlsfx.glyphfont.FontAwesome.Glyph.PLUS

plugin {
    //Editables
    editable(::ExpandableSExpr) { expr ->
        ExpandableSExpr().also {
            val editable = platform.getEditable(expr) as EditableSExpr<*>
            it.setContent(editable)
        }
    }
    //Editors
    editor(::IntLiteralEditor)
    editor(::ApplyEditor)
    editor(::CharLiteralEditor)
    editor(::DoubleLiteralEditor)
    editor(::StringLiteralEditor)
    editor(::GetValEditor)
    editor(::SExprExpander)
    editor(::IdentifierEditor)
    editor(::SExprListEditor)
    //Views
    view(::IntLiteralEditorControl)
    view(::CharLiteralEditorControl)
    view(::DoubleLiteralEditorControl)
    view(::GetValEditorControl)
    view(::StringLiteralEditorControl)
    view(::ApplyEditorControl)
    view<EditableSExprList, FXListEditorView> { editable, ctx, args ->
        FXListEditorView.withAltGlyph(editable, ctx, glyph = PLUS, args = args)
    }
}