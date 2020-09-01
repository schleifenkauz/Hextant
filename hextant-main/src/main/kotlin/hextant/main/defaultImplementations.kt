/**
 * @author Nikolaus Knop
 */

package hextant.main

import hextant.context.ControlFactory
import hextant.main.editor.*
import hextant.main.view.*
import hextant.plugin.Aspects

internal fun Aspects.registerDefaultImplementations() {
    implement(ControlFactory::class, DisabledPluginInfoEditor::class, DisabledPluginInfoEditorControlFactory)
    implement(ControlFactory::class, EnabledPluginInfoEditor::class, EnabledPluginInfoEditorControlFactory)
    implement(ControlFactory::class, PluginsEditor::class, PluginsEditorControlFactory)
}