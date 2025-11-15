package hextant.launcher

import bundles.PublicProperty
import bundles.property
import hextant.cli.CLI
import hextant.cli.HextantDirectory
import hextant.command.meta.extract
import hextant.completion.CompletionStrategy
import hextant.completion.NoCompleter
import hextant.context.Context
import hextant.context.Properties.marketplace
import hextant.core.editor.SimpleStringEditor
import hextant.core.view.CompoundEditorControl
import hextant.core.view.ExpanderControl
import hextant.core.view.TokenEditorControl
import hextant.fx.ConsoleOutputView
import hextant.launcher.editor.*
import hextant.launcher.plugins.PluginSource
import hextant.launcher.plugins.Plugins
import hextant.launcher.view.ProjectNameCompleter
import hextant.launcher.view.ProjectTypeCompleter
import hextant.plugins.*
import kotlinx.coroutines.runBlocking
import reaktive.value.binding.impl.notNull
import reaktive.value.binding.map
import reaktive.value.now
import java.io.File

internal object HextantLauncher : PluginInitializer({
    registerControlFactory { editor: ProjectTypeEditor, arguments ->
        TokenEditorControl(
            editor,
            arguments,
            ProjectTypeCompleter
        )
    }

    registerControlFactory { editor: ProjectNameEditor, arguments ->
        val completer = if (editor.isCreate) NoCompleter else ProjectNameCompleter
        TokenEditorControl(editor, arguments, completer)
    }

    registerControlFactory { editor: MavenPluginSourceEditor, arguments ->
        CompoundEditorControl(editor, arguments) {
            horizontal {
                view(editor.group)
                operator(":")
                view(editor.artifact)
            }
        }
    }

    registerControlFactory { editor: PluginSourceExpander, arguments ->
        ExpanderControl(editor, arguments, PluginSourceExpander.config.completer(CompletionStrategy.simple))
    }

    registerControlFactory { editor: URLPluginSourceEditor, arguments ->
        TokenEditorControl(editor, arguments, styleClass = "plugin-url")
    }

    registerControlFactory { editor: GitHubPluginSourceEditor, arguments ->
        CompoundEditorControl(editor, arguments) {
            horizontal {
                operator("https://github.com/")
                view(editor.userName)
                operator("/")
                view(editor.repository)
            }
        }
    }
    configurableProperty(Header, ::SimpleStringEditor)

    commandDelegation<Context> { ctx -> if (ctx.hasProperty(Launcher)) ctx[Launcher] else null }
    registerCommand<Launcher, String> {
        extract(Launcher::create)
        parameter("dest").ofType<File>().apply {
            editWith { ProjectNameEditor(isCreate = true) }
        }
    }
    registerCommand<Launcher, String> {
        extract(Launcher::open)
        parameter("project").ofType<File>().apply {
            editWith { ProjectNameEditor(isCreate = false) }
        }
    }
    registerCommand<Launcher, String> {
        extract(Launcher::delete)
        parameter("project").ofType<File>().apply {
            editWith { ProjectNameEditor(isCreate = false) }
        }
    }
    registerCommand<Launcher, String> {
        extract(Launcher::rename)
        parameter("project").ofType<File>().apply {
            editWith { ProjectNameEditor(isCreate = false) }
        }
        parameter("newLocation").ofType<File>().apply {
            editWith { ProjectNameEditor(isCreate = true) }
        }
    }
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
                    val jar = HextantDirectory["plugins/$p.jar"]
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
        description =
            "Checks that a project referenced by the user is not currently used by another Hextant window"
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
