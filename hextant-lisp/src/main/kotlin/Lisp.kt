import hextant.core.view.ListEditorControl
import hextant.core.view.ListEditorControl.Orientation.Horizontal
import hextant.lisp.editor.*
import hextant.lisp.view.*
import hextant.plugin.dsl.PluginInitializer
import org.controlsfx.glyphfont.FontAwesome.Glyph.PLUS

object Lisp : PluginInitializer({
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
    view<SExprListEditor, ListEditorControl> { editable, args ->
        ListEditorControl.withAltGlyph(editable, glyph = PLUS, args = args, orientation = Horizontal)
    }
})