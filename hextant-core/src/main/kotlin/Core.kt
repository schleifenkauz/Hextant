import hextant.completion.CompletionStrategy
import hextant.config.*
import hextant.core.editor.*
import hextant.core.inspect.SyntaxErrorInspection
import hextant.core.view.*
import hextant.core.view.ValidatedTokenEditorControl.Companion.ABORT_CHANGE
import hextant.core.view.ValidatedTokenEditorControl.Companion.BEGIN_CHANGE
import hextant.core.view.ValidatedTokenEditorControl.Companion.COMMIT_CHANGE
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
    view(::ExpanderControl)
    view { e: ListEditor<*, *>, args ->
        ListEditorControl(e, args)
    }
    view { e: TokenEditor<*>, args -> TokenEditorControl(e, args) }
    inspection(SyntaxErrorInspection)
    registerInspection<ValidatedTokenEditor<*>> {
        id = "invalid-intermediate"
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
        ValidatedTokenEditorControl(e, bundle)
    }
    view { e: RootExpander<*>, bundle ->
        val completer = e.context[ProjectItemEditor.expanderConfig<Any?>()].completer(CompletionStrategy.simple)
        ExpanderControl(e, bundle, completer)
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
    view(::ValidatedTokenEditorControl)
    view(::PathEditorControl)
    stylesheet("hextant/core/style.css")
    defaultEditor(::EnabledEditor)
    view { e: EnabledEditor, args ->
        args[TokenEditorControl.COMPLETER] = EnabledCompleter
        TokenEditorControl(e, args)
    }
    command(enable)
    command(disable)
})