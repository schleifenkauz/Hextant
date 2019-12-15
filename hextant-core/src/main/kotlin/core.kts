import hextant.core.editor.*
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.*
import hextant.createView
import hextant.plugin.dsl.plugin

plugin {
    name = "Hextant Core"
    author = "Nikolaus Knop"
    view(::FXExpanderView)
    view { e: ListEditor<*, *>, args -> FXListEditorView(e, args) }
    view { e: TokenEditor<*, TokenEditorView>, args -> FXTokenEditorView(e, args) }
    inspection(::SyntaxErrorInspection)
    view { e: TransformedEditor<*, *>, bundle -> e.context.createView(e.source, bundle) }
    stylesheet("hextant/core/style.css")
}