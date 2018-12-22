import hextant.core.expr.editable.EditableIntLiteral
import hextant.core.expr.editor.IntLiteralEditor
import hextant.plugin.dsl.plugin

plugin {
    author = "Nikolaus Knop"
    name = "Hextant"
    editable(::EditableIntLiteral, ::EditableIntLiteral)
    editor(::IntLiteralEditor)
}