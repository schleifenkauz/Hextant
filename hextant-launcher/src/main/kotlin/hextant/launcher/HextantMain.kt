package hextant.launcher

import bundles.PublicProperty
import bundles.property
import hextant.context.Context
import hextant.context.Properties.marketplace
import hextant.core.editor.SimpleStringEditor
import hextant.fx.ConsoleOutputView
import hextant.install.CLI
import hextant.install.Plugins
import hextant.launcher.editor.ProjectNameEditor
import hextant.plugins.PluginInitializer
import hextant.plugins.configurableProperty
import hextant.plugins.registerCommand
import hextant.plugins.registerInspection
import hextant.main.HextantDirectory
import kotlinx.coroutines.runBlocking
import reaktive.value.binding.impl.notNull
import reaktive.value.binding.map
import reaktive.value.now

internal object HextantMain : PluginInitializer({
    configurableProperty(Header, ::SimpleStringEditor)
    //TODO("more project manager commands")
    registerCommand<Context, Unit> {
        name = "Install plugin"
        shortName = "install"
        description = "Installs or updates a plugin"
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
                    val jar = context[HextantDirectory]["plugins/$p.jar"]
                    if (jar.exists()) {
                        runBlocking { context[marketplace].upload(jar) }
                    }
                },
                prematureExit = { CLI.destroyAllChildProcesses() }
            )
        }
    }
    registerInspection<ProjectNameEditor> {
        id = "project-name.format"
        description = "Ensures that the name of a project being created is valid"
        isSevere(true)
        checkingThat { inspected.result.notNull() }
        message { "Invalid project name '${inspected.text.now}'" }
    }
    registerInspection<ProjectNameEditor> {
        id = "project-name.already-exists"
        description = "Checks that the location of a project the user attempts to create doesn't exist yet"
        appliesIf { inspected.isCreate }
        isSevere(true)
        preventingThat {
            inspected.result.map { path ->
                path != null && path.isDirectory
            }
        }
        message { "Project with name '${inspected.result.now!!.name}' already exists" }
    }
    registerInspection<ProjectNameEditor> {
        id = "project-location.check-ref"
        description = "Checks that the location of a project the user attempts to reference does exist"
        appliesIf { !inspected.isCreate }
        isSevere(true)
        checkingThat {
            inspected.result.map { path ->
                path == null || path.resolve(HextantDirectory.PROJECT_INFO).isFile
            }
        }
        message { "Project with name '${inspected.result.now!!.name}' not found" }
    }
    registerInspection<ProjectNameEditor> {
        id = "project-location.locked"
        description = "Checks that a project referenced by the user is not currently used by another Hextant window"
        appliesIf { !inspected.isCreate }
        isSevere(true)
        preventingThat {
            inspected.result.map { path ->
                path != null && path.resolve(HextantDirectory.LOCK).isFile
            }
        }
        message { "Project '${inspected.result.now!!.name}' is currently opened" }
    }
}) {
    object Header : PublicProperty<String> by property("header", default = "Hextant")
}