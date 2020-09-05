/**
 * @author Nikolaus Knop
 */

package hextant.main.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.TokenEditorControl
import hextant.main.editor.*

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