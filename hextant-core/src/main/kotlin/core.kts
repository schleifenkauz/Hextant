import hextant.command.line.CommandLineController
import hextant.command.line.FXCommandLineView
import hextant.core.editor.TextEditor
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.list.EditableList
import hextant.core.list.FXListEditorView
import hextant.core.view.FXExpanderView
import hextant.core.view.FXTextEditorView
import hextant.plugin.dsl.plugin

plugin {
    name = "Hextant"
    author = "Nikolaus Knop"
    editor(::TextEditor)
    view(::FXTextEditorView)
    //Lists
    view { e: EditableList<*, *>, ctx, args -> FXListEditorView(e, ctx, bundle = args) }
    //Expanders
    view(::FXExpanderView)
    //Command Line
    editor(::CommandLineController)
    view(::FXCommandLineView)
    inspection(::SyntaxErrorInspection)
}