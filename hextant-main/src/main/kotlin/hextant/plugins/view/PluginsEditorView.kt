/**
 * @author Nikolaus Knop
 */

package hextant.plugins.view

import hextant.core.EditorView
import hextant.plugins.Plugin
import hextant.plugins.PluginManager

internal interface PluginsEditorView : EditorView {
    val available: MutableCollection<Plugin>
    val enabled: MutableCollection<Plugin>

    suspend fun confirmEnable(enabled: Collection<Plugin>): Boolean

    suspend fun confirmDisable(disabled: Collection<Plugin>): Boolean

    suspend fun askDisable(plugin: Plugin): PluginManager.DisableConfirmation

    fun alertError(message: String)

    val availableSearchText: String

    val enabledSearchText: String
}