package hextant.launcher.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.completion.CompletionStrategy
import hextant.completion.NoCompleter
import hextant.context.ControlFactory
import hextant.core.view.CompoundEditorControl
import hextant.core.view.ExpanderControl
import hextant.core.view.TokenEditorControl
import hextant.launcher.editor.*
import hextant.launcher.editor.ProjectNameEditor
import hextant.launcher.editor.ProjectTypeEditor

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: ProjectTypeEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, ProjectTypeCompleter)

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: ProjectNameEditor, arguments: Bundle): TokenEditorControl {
    val completer = if (editor.isCreate) NoCompleter else ProjectNameCompleter
    return TokenEditorControl(editor, arguments, completer)
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: MavenPluginSourceEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.group)
        operator(":")
        view(editor.artifact)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: PluginSourceExpander, arguments: Bundle) =
    ExpanderControl(editor, arguments, PluginSourceExpander.config.completer(CompletionStrategy.simple))

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: URLPluginSourceEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "plugin-url")


@ProvideImplementation(ControlFactory::class)
fun createControl(editor: GitHubPluginSourceEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        operator("https://github.com/")
        view(editor.userName)
        operator("/")
        view(editor.repository)
    }
}