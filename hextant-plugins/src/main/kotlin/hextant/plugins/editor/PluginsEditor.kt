/**
 *@author Nikolaus Knop
 */

package hextant.plugins.editor

import hextant.context.Context
import hextant.core.editor.AbstractEditor
import hextant.plugins.*
import hextant.plugins.Plugin.Type
import hextant.plugins.view.PluginsEditorView
import kotlinx.coroutines.*
import reaktive.value.reactiveValue
import validated.valid

class PluginsEditor(
    context: Context,
    private val plugins: PluginManager,
    private val marketplace: Marketplace,
    private val types: Set<Type>
) : AbstractEditor<Set<Plugin>, PluginsEditorView>(context) {
    override val result get() = reactiveValue(valid(plugins.enabledPlugins()))

    suspend fun enable(plugin: Plugin, view: PluginsEditorView) {
        val activated = try {
            plugins.enable(plugin, view::confirmEnable) ?: return
        } catch (e: PluginException) {
            return view.alertError(e.message!!)
        }
        for (pl in activated) {
            GlobalScope.launch {
                marketplace.download(pl.id)
            }
        }
        views {
            available.removeAll(activated)
            enabled.addAll(activated.filter { it.matches(enabledSearchText) })
        }
    }

    suspend fun disable(plugin: Plugin, view: PluginsEditorView) {
        val disabled = plugins.disable(plugin, view::confirmDisable, view::askDisable) ?: return
        views {
            available.addAll(disabled.filter { it.matches(availableSearchText) })
            enabled.removeAll(disabled)
        }
    }

    suspend fun searchInAvailable(view: PluginsEditorView) {
        val available = marketplace.getPlugins(view.availableSearchText, LIMIT, types, plugins.enabledIds())
        view.available.clear()
        view.available.addAll(available)
    }

    fun searchInEnabled(view: PluginsEditorView) {
        val enabled = plugins.enabledPlugins().filter { it.matches(view.enabledSearchText) }
        view.enabled.clear()
        view.enabled.addAll(enabled)
    }

    override fun viewAdded(view: PluginsEditorView) {
        searchInEnabled(view)
        runBlocking { searchInAvailable(view) }
    }

    private fun Plugin.matches(searchText: String): Boolean {
        if (type !in types) return false
        return name.matches(searchText) ||
                author.matches(searchText) ||
                id.matches(searchText) ||
                projectTypes.any { it.name.matches(searchText) }
    }

    private fun String.matches(searchText: String) = startsWith(searchText)

    companion object {
        private const val LIMIT = 20
    }
}