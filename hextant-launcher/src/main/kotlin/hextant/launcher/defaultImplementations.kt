/**
 * @author Nikolaus Knop
 */

package hextant.launcher

import hextant.context.ControlFactory
import hextant.launcher.editor.*
import hextant.launcher.view.*
import hextant.plugin.Aspects

internal fun Aspects.registerDefaultImplementations() {
    implement(ControlFactory::class, DisabledPluginInfoEditor::class, DisabledPluginInfoEditorControlFactory)
    implement(ControlFactory::class, EnabledPluginInfoEditor::class, EnabledPluginInfoEditorControlFactory)
    implement(ControlFactory::class, PluginsEditor::class, PluginsEditorControlFactory)
}