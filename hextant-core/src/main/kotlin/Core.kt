import hextant.completion.CompletionStrategy
import hextant.completion.ConfiguredCompleter
import hextant.config.*
import hextant.core.editor.*
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.*
import hextant.core.view.FilteredTokenEditorControl.Companion.ABORT_CHANGE
import hextant.core.view.FilteredTokenEditorControl.Companion.BEGIN_CHANGE
import hextant.core.view.FilteredTokenEditorControl.Companion.COMMIT_CHANGE
import hextant.createView
import hextant.fx.shortcut
import hextant.main.PathEditorControl
import hextant.plugin.dsl.PluginInitializer
import hextant.project.editor.FileEditor.RootExpander
import hextant.project.editor.FileNameEditor
import hextant.project.editor.ProjectItemEditor
import hextant.project.view.DirectoryEditorControl
import hextant.project.view.FileEditorControl
import hextant.settings.editors.*
import javafx.scene.input.KeyCode.*
import reaktive.value.binding.map
import reaktive.value.now
import validated.Validated.Invalid
import validated.isValid

/**
 * The core plugin registers basic editors, views and commands.
 */
object Core : PluginInitializer({
    name = "Hextant Core"
    author = "Nikolaus Knop"
    editor(::StringEditor)
    defaultEditor(::StringEditor)
    view(::FXExpanderView)
    view { e: ListEditor<*, *>, args ->
        ListEditorControl(e, args)
    }
    view { e: TokenEditor<*, TokenEditorView>, args -> FXTokenEditorView(e, args) }
    inspection(SyntaxErrorInspection)
    registerInspection<FilteredTokenEditor<*>> {
        description = "Reports invalid intermediate result"
        checkingThat { inspected.intermediateResult.map { it.isValid } }
        message { (inspected.intermediateResult.now as Invalid).reason }
        isSevere(true)
    }
    view { e: TransformedEditor<*, *>, bundle -> e.context.createView(e.source, bundle) }
    view { e: FileNameEditor, bundle ->
        bundle[BEGIN_CHANGE] = shortcut(F2)
        bundle[ABORT_CHANGE] = shortcut(ESCAPE)
        bundle[COMMIT_CHANGE] = shortcut(ENTER)
        FilteredTokenEditorControl(e, bundle)
    }
    view { e: RootExpander<*>, bundle ->
        val completer = e.context[ProjectItemEditor.expanderConfig<Any?>()].completer(CompletionStrategy.simple)
        FXExpanderView(e, bundle, completer)
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
    view { e: FileNameEditor, args ->
        args[FilteredTokenEditorControl.COMPLETER] =
            ConfiguredCompleter.withStringPool(CompletionStrategy.simple, listOf("file"))
        FilteredTokenEditorControl(e, args)
    }
    view(::DirectoryEditorControl)
    view(::FilteredTokenEditorControl)
    view(::PathEditorControl)
    stylesheet("hextant/core/style.css")
    defaultEditor(::EnabledEditor)
    view { e: EnabledEditor, args ->
        args[AbstractTokenEditorControl.COMPLETER] = EnabledCompleter
        FXTokenEditorView(e, args)
    }
    command(enable)
    command(disable)
})