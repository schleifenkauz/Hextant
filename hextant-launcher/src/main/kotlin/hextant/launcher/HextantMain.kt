package hextant.launcher

import bundles.PublicProperty
import bundles.property
import hextant.context.Context
import hextant.core.editor.SimpleStringEditor
import hextant.fx.ConsoleOutputView
import hextant.install.CLI
import hextant.install.Plugins
import hextant.launcher.HextantPlatform.marketplace
import hextant.plugin.*
import hextant.plugins.PluginInfo
import kotlinx.coroutines.runBlocking

internal object HextantMain : PluginInitializer({
    commandDelegation { ctx: Context -> if (ctx.hasProperty(ProjectManager)) ctx[ProjectManager] else null }
    configurableProperty(Header, ::SimpleStringEditor)
    registerCommand(showPluginManager(setOf(PluginInfo.Type.Global)))
    registerCommand(enablePlugin(setOf(PluginInfo.Type.Global)))
    registerCommand(disablePlugin)
    registerCommand<Context, Unit> {
        name = "Install plugin"
        shortName = "install"
        description = "Install or updates a plugin"
        val plugin = addParameter<PluginSource> {
            name = "plugin"
            description = "The plugin that should be installed or updated"
        }
        executing { context, args ->
            ConsoleOutputView(context).execute(
                action = {
                    val p = args[plugin]
                    when (p) {
                        is PluginSource.GitRepo -> Plugins.installOrUpdatePluginFromSource(p.url.toExternalForm())
                        is PluginSource.MavenCoordinate -> Plugins.installOrUpdateFromMaven(p.group, p.artifact)
                    }
                    val jar = context[Files]["plugins/$p.jar"]
                    if (jar.exists()) {
                        runBlocking { context[marketplace].upload(jar) }
                    }
                },
                prematureExit = { CLI.destroyAllChildProcesses() }
            )
        }
    }
}) {
    object Header : PublicProperty<String> by property("header", default = "Hextant")
}