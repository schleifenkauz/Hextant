import hextant.*

import hextant.core.editor.*
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.*
import hextant.core.view.FilteredTokenEditorControl.Companion.ABORT_CHANGE
import hextant.core.view.FilteredTokenEditorControl.Companion.BEGIN_CHANGE
import hextant.core.view.FilteredTokenEditorControl.Companion.COMMIT_CHANGE
import hextant.fx.shortcut
import hextant.main.PathEditorControl
import hextant.plugin.dsl.plugin
import hextant.project.editor.FileNameEditor
import hextant.project.view.DirectoryEditorControl
import hextant.project.view.FileEditorControl
import hextant.settings.editors.*
import javafx.scene.input.KeyCode.*
import reaktive.value.binding.map
import reaktive.value.now

plugin {
    name = "Hextant Core"
    author = "Nikolaus Knop"
    editor(::StringEditor)
    defaultEditor(::StringEditor)
    view(::FXExpanderView)
    view { e: ListEditor<*, *>, args -> ListEditorControl(e, args) }
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
        bundle[BEGIN_CHANGE] = shortcut(F2)
        bundle[ABORT_CHANGE] = shortcut(ESCAPE)
        bundle[COMMIT_CHANGE] = shortcut(ENTER)
        FilteredTokenEditorControl(e, bundle)
    }
    compoundView { e: SettingsEntryEditor ->
        line {
            keyword(e.property.property.name!!)
            space()
            operator("=")
            space()
            view(e.value)
        }
    }
    view { e: SettingsEntryListEditor, bundle ->
        bundle[ListEditorControl.ORIENTATION] = ListEditorControl.Orientation.Vertical
        ListEditorControl.withAltText(e, "Configure property", bundle)
    }
    compoundView { e: SettingsEditor -> view(e.entries) }
    view(::FileEditorControl)
    view(::DirectoryEditorControl)
    view(::FilteredTokenEditorControl)
    view(::PathEditorControl)
    stylesheet("hextant/core/style.css")
}