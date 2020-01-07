import hextant.*
import hextant.bundle.CorePermissions.Public
import hextant.core.editor.*
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.*
import hextant.core.view.FilteredTokenEditorControl.Companion.ABORT_CHANGE
import hextant.core.view.FilteredTokenEditorControl.Companion.BEGIN_CHANGE
import hextant.core.view.FilteredTokenEditorControl.Companion.COMMIT_CHANGE
import hextant.fx.shortcut
import hextant.plugin.dsl.plugin
import hextant.project.editor.FileNameEditor
import hextant.project.view.DirectoryEditorControl
import hextant.project.view.FileEditorControl
import javafx.scene.input.KeyCode.*
import reaktive.value.binding.map
import reaktive.value.now

plugin {
    name = "Hextant Core"
    author = "Nikolaus Knop"
    view(::FXExpanderView)
    view { e: ListEditor<*, *>, args -> FXListEditorView(e, args) }
    view { e: TokenEditor<*, TokenEditorView>, args -> FXTokenEditorView(e, args) }
    inspection(::SyntaxErrorInspection)
    registerInspection<FilteredTokenEditor<*>> {
        description = "Reports invalid intermediate result"
        checkingThat(inspected.intermediateResult.map { it.isOk })
        message { (inspected.intermediateResult.now as Err).message }
        isSevere(true)
    }
    view { e: TransformedEditor<*, *>, bundle -> e.context.createView(e.source, bundle) }
    view { e: FileNameEditor, bundle ->
        bundle[Public, BEGIN_CHANGE] = shortcut(F2)
        bundle[Public, ABORT_CHANGE] = shortcut(ESCAPE)
        bundle[Public, COMMIT_CHANGE] = shortcut(ENTER)
        FilteredTokenEditorControl(e, bundle)
    }
    view(::FileEditorControl)
    view(::DirectoryEditorControl)
    view(::FilteredTokenEditorControl)
    stylesheet("hextant/core/style.css")
}