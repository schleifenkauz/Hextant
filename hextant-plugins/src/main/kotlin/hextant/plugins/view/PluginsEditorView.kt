/**
 * @author Nikolaus Knop
 */

package hextant.plugins.view

import hextant.core.EditorView
import hextant.plugins.Plugin
import hextant.plugins.PluginManager

interface PluginsEditorView : EditorView {
    val available: MutableCollection<Plugin>
    val enabled: MutableCollection<Plugin>

    fun confirmEnable(enabled: Collection<Plugin>): Boolean

    fun confirmDisable(disabled: Collection<Plugin>): Boolean

    fun askDisable(plugin: Plugin): PluginManager.DisableConfirmation

    fun alertError(message: String)

    val availableSearchText: String

    val enabledSearchText: String
}