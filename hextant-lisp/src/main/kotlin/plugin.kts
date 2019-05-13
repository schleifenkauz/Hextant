import hextant.core.view.FXListEditorView
import hextant.core.view.FXListEditorView.Orientation.Horizontal
import hextant.lisp.editor.*
import hextant.lisp.view.*
import hextant.plugin.dsl.plugin
import org.controlsfx.glyphfont.FontAwesome.Glyph.PLUS

plugin {
    //Editors
    defaultEditor(::IntLiteralEditor)
    defaultEditor(::ApplyEditor)
    defaultEditor(::CharLiteralEditor)
    defaultEditor(::DoubleLiteralEditor)
    defaultEditor(::StringLiteralEditor)
    defaultEditor(::SExprExpander)
    defaultEditor(::IdentifierEditor)
    defaultEditor(::SExprListEditor)
    //Views
    view(::IntLiteralEditorControl)
    view(::CharLiteralEditorControl)
    view(::DoubleLiteralEditorControl)
    view(::GetValEditorControl)
    view(::StringLiteralEditorControl)
    view(::ApplyEditorControl)
    view<SExprListEditor, FXListEditorView> { editable, args ->
        FXListEditorView.withAltGlyph(editable, glyph = PLUS, args = args, orientation = Horizontal)
    }
}