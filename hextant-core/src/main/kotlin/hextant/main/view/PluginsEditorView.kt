/**
 * @author Nikolaus Knop
 */

package hextant.main.view

import hextant.core.EditorView
import hextant.main.plugins.PluginManager
import hextant.plugins.Plugin

internal interface PluginsEditorView : EditorView {
    val available: MutableCollection<Plugin>
    val enabled: MutableCollection<Plugin>

    fun confirmEnable(enabled: Collection<Plugin>): Boolean

    fun confirmDisable(disabled: Collection<Plugin>): Boolean

    fun askDisable(plugin: Plugin): PluginManager.DisableConfirmation

    fun alertError(message: String)

    val availableSearchText: String

    val enabledSearchText: String
}