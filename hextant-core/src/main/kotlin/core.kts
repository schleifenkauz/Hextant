import hextant.bundle.CorePermissions.Public
import hextant.core.editor.*
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.*
import hextant.createView
import hextant.fx.shortcut
import hextant.plugin.dsl.plugin
import hextant.project.editor.FileNameEditor
import hextant.project.view.*
import javafx.scene.input.KeyCode.*

plugin {
    name = "Hextant Core"
    author = "Nikolaus Knop"
    view(::FXExpanderView)
    view { e: ListEditor<*, *>, args -> FXListEditorView(e, args) }
    view { e: TokenEditor<*, TokenEditorView>, args -> FXTokenEditorView(e, args) }
    inspection(::SyntaxErrorInspection)
    view { e: TransformedEditor<*, *>, bundle -> e.context.createView(e.source, bundle) }
    view { e: FileNameEditor, bundle ->
        bundle[Public, TokenEditorControl.BEGIN_CHANGE] = shortcut(F2)
        bundle[Public, TokenEditorControl.ABORT_CHANGE] = shortcut(ESCAPE)
        bundle[Public, TokenEditorControl.COMMIT_CHANGE] = shortcut(ENTER)
        TokenEditorControl(e, bundle)
    }
    view(::FileEditorControl)
    view(::DirectoryEditorControl)
    view(::ProjectItemExpanderView)
    stylesheet("hextant/core/style.css")
}