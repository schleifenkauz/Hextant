/**
 * @author Nikolaus Knop
 */

package hextant.launcher.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.completion.CompletionStrategy
import hextant.context.ControlFactory
import hextant.core.view.*
import hextant.launcher.editor.*

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: ProjectTypeEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, ProjectTypeCompleter)

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: ProjectLocationEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, ProjectLocationCompleter)

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: ProjectNameEditor, arguments: Bundle) = TokenEditorControl(editor, arguments)

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: EnabledPluginInfoEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, EnabledPluginInfoCompleter)

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: DisabledPluginInfoEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, DisabledPluginInfoCompleter(editor.types))

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: PluginSourceExpander, arguments: Bundle) =
    ExpanderControl(editor, arguments, PluginSourceExpander.config.completer(CompletionStrategy.simple))

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: URLPluginSourceEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, styleClass = "plugin-url")

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: MavenPluginSourceEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        view(editor.group)
        operator(":")
        view(editor.artifact)
    }
}

@ProvideImplementation(ControlFactory::class)
fun createControl(editor: GitHubPluginSourceEditor, arguments: Bundle) = CompoundEditorControl(editor, arguments) {
    line {
        operator("https://github.com/")
        view(editor.userName)
        operator("/")
        view(editor.repository)
    }
}