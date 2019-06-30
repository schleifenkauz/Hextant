import hextant.core.editor.ListEditor
import hextant.core.editor.TokenEditor
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.*
import hextant.plugin.dsl.plugin

plugin {
    name = "Hextant Core"
    author = "Nikolaus Knop"
    view(::FXExpanderView)
    view { e: ListEditor<*, *>, args -> FXListEditorView(e, args) }
    view { e: TokenEditor<*, TokenEditorView>, args -> FXTokenEditorView(e, args) }
    inspection(::SyntaxErrorInspection)
    stylesheet("hextant/core/style.css")
}