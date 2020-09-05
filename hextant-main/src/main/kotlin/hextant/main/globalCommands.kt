/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.command.*
import hextant.command.Command.Type.SingleReceiver
import hextant.context.Context
import hextant.context.createControl
import hextant.fx.showStage
import hextant.main.HextantPlatform.launcher
import hextant.main.editor.*
import hextant.main.plugins.PluginManager
import hextant.main.view.PluginsEditorView
import hextant.plugins.PluginInfo
import hextant.plugins.PluginInfo.Type.Global
import hextant.plugins.PluginInfo.Type.Local

internal fun Commands.registerGlobalCommands(context: Context) {
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
            closeProject(ctx)
            ctx[Project].save()
            val loader = context.get<Runnable>(launcher)
            loader.run()
        }
    }
    register(showPluginManager(setOf(Local, Global)))
    register(enablePlugin(setOf(Local, Global)))
    register(disablePlugin)
}

internal fun showPluginManager(types: Set<PluginInfo.Type>) = command<Context, Unit> {
    name = "Show plugin manager"
    shortName = "plugins"
    description = "Shows the plugin manager"
    type = SingleReceiver
    defaultShortcut("Ctrl+P")
    applicableIf { ctx -> ctx.hasProperty(PluginManager) }
    executing { ctx, _ ->
        val manager = ctx[PluginManager]
        val editor = PluginsEditor(ctx, manager, types)
        showStage(editor)
    }
}

internal fun enablePlugin(types: Set<PluginInfo.Type>) = command<Context, Unit> {
    name = "Enable plugin"
    shortName = "enable-plugin"
    description = "Enables a plugin"
    type = SingleReceiver
    applicableIf { ctx -> ctx.hasProperty(PluginManager) }
    addParameter<PluginInfo> {
        name = "plugin"
        description = "The plugin that should be enabled"
        editWith { ctx -> DisabledPluginInfoEditor(ctx, types) }
    }
    executing { context, (plugin) ->
        plugin as PluginInfo
        val manager = context[PluginManager]
        val editor = PluginsEditor(context, manager, emptySet())
        val view = context.createControl(editor) as PluginsEditorView
        editor.enable(manager.getPlugin(plugin.id), view)
    }
}

internal val disablePlugin = command<Context, Unit> {
    name = "Disable plugin"
    shortName = "disable-plugin"
    description = "Disables a plugin"
    type = SingleReceiver
    applicableIf { ctx -> ctx.hasProperty(PluginManager) }
    addParameter<PluginInfo> {
        name = "plugin"
        description = "The plugin that should be disabled"
        editWith<EnabledPluginInfoEditor>()
    }
    executing { context, (plugin) ->
        plugin as PluginInfo
        val manager = context[PluginManager]
        val editor = PluginsEditor(context, manager, emptySet())
        val view = context.createControl(editor) as PluginsEditorView
        editor.disable(manager.getPlugin(plugin.id), view)
    }
}