package hextant.main

import hextant.cli.HextantDirectory
import hextant.command.Command
import hextant.command.Command.Type.SingleReceiver
import hextant.context.Context
import hextant.context.createControl
import hextant.fx.showStage
import hextant.plugins.*
import hextant.plugins.editor.DisabledPluginInfoEditor
import hextant.plugins.editor.EnabledPluginInfoEditor
import hextant.plugins.editor.PluginsEditor
import hextant.plugins.view.PluginsEditorView

object HextantMain : PluginInitializer({
    registerCommand<Context, Unit> {
        name = "Save Project"
        shortName = "save"
        description = "Saves the project"
        type = SingleReceiver
        defaultShortcut("Ctrl+S")
        applicableIf { ctx: Context -> ctx.hasProperty(Project) }
        executing { ctx: Context, _ ->
            ctx[Project].save()
        }
    }
    registerCommand<Context, Unit> {
        name = "Quit"
        shortName = "quit"
        description = "Saves and closes the current project and opens the launcher"
        type = SingleReceiver
        defaultShortcut("Ctrl+Q")
        applicableIf { ctx -> ctx.hasProperty(Project) }
        executing { ctx, _ ->
            ctx[Project].save()
            HextantDirectory.releaseLock(ctx[Project].location)
            ctx[HextantApp.mainWindow].close()
        }
    }
    registerCommand<Context, Unit> {
        name = "Show plugin manager"
        shortName = "plugins"
        description = "Shows the plugin manager"
        type = SingleReceiver
        defaultShortcut("Ctrl+P")
        applicableIf { ctx -> ctx.hasProperty(PluginManager) }
        executing { ctx, _ ->
            val manager = ctx[PluginManager]
            val editor = PluginsEditor(ctx, manager, setOf(PluginInfo.Type.Local, PluginInfo.Type.Global))
            showStage(editor, applyStyle = false)
        }
    }
    registerCommand<Context, Unit> {
        name = "Enable plugin"
        shortName = "enable-plugin"
        description = "Enables a plugin"
        type = SingleReceiver
        applicableIf { ctx -> ctx.hasProperty(PluginManager) }
        val plugin = addParameter<PluginInfo> {
            name = "plugin"
            description = "The plugin that should be enabled"
            editWith { ctx -> DisabledPluginInfoEditor(ctx, setOf(PluginInfo.Type.Local, PluginInfo.Type.Global)) }
        }
        executing { context, args ->
            val manager = context[PluginManager]
            val editor = PluginsEditor(context, manager, emptySet())
            val view = context.createControl(editor) as PluginsEditorView
            editor.enable(manager.getPlugin(args[plugin].id), view)
        }
    }
    registerCommand<Context, Unit> {
        name = "Disable plugin"
        shortName = "disable-plugin"
        description = "Disables a plugin"
        type = SingleReceiver
        applicableIf { ctx -> ctx.hasProperty(PluginManager) }
        val plugin = addParameter<PluginInfo> {
            name = "plugin"
            description = "The plugin that should be disabled"
            editWith<EnabledPluginInfoEditor>()
        }
        executing { context, args ->
            val manager = context[PluginManager]
            val editor = PluginsEditor(context, manager, emptySet())
            val view = context.createControl(editor) as PluginsEditorView
            editor.disable(manager.getPlugin(args[plugin].id), view)
        }
    }
})