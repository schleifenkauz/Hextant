package hextant.main

import hextant.core.view.AbstractTokenEditorControl
import hextant.main.editors.*
import hextant.plugin.PluginInitializer

object Main : PluginInitializer({
    defaultEditor(::ProjectLocationEditor)
    defaultEditor(::ProjectTypeEditor)
    view { e: ProjectLocationEditor, args ->
        args[AbstractTokenEditorControl.COMPLETER] = ProjectLocationCompleter
        ProjectLocationEditorControl(e, args)
    }
    view(::PluginsEditorControl)
    tokenEditorView<ProjectTypeEditor>(completer = ProjectTypeCompleter)
})