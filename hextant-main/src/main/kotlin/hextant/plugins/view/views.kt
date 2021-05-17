/**
 * @author Nikolaus Knop
 */

package hextant.plugins.view

import bundles.Bundle
import hextant.codegen.ProvideImplementation
import hextant.context.ControlFactory
import hextant.core.view.TokenEditorControl
import hextant.plugins.editor.DisabledPluginInfoEditor
import hextant.plugins.editor.EnabledPluginInfoEditor

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: EnabledPluginInfoEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, EnabledPluginInfoCompleter)

@ProvideImplementation(ControlFactory::class)
internal fun createControl(editor: DisabledPluginInfoEditor, arguments: Bundle) =
    TokenEditorControl(editor, arguments, DisabledPluginInfoCompleter(editor.types))
