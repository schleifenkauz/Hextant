/**
 *@author Nikolaus Knop
 */

package hextant.main.editor

import hextant.context.Context
import hextant.core.editor.AbstractEditor
import hextant.main.plugins.PluginException
import hextant.main.plugins.PluginManager
import hextant.main.view.PluginsEditorView
import hextant.plugins.Marketplace
import hextant.plugins.Plugin
import hextant.plugins.Plugin.Type
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import reaktive.value.reactiveValue
import validated.valid

internal class PluginsEditor(
    context: Context,
    private val plugins: PluginManager,
    private val marketplace: Marketplace,
    private val types: Set<Type>
) : AbstractEditor<Set<String>, PluginsEditorView>(context) {
    override val result get() = reactiveValue(valid(plugins.enabledIds()))

    fun enable(plugin: Plugin, view: PluginsEditorView) {
        val activated = try {
            plugins.enable(plugin, view::confirmEnable) ?: return
        } catch (e: PluginException) {
            return view.alertError(e.message!!)
        }
        for (pl in activated) {
            GlobalScope.launch {
                marketplace.getJarFile(pl.id)
            }
        }
        views {
            available.removeAll(activated)
            enabled.addAll(activated.filter { it.matches(enabledSearchText) })
        }
    }

    fun disable(plugin: Plugin, view: PluginsEditorView) {
        val disabled = plugins.disable(plugin, view::confirmDisable, view::askDisable) ?: return
        views {
            available.addAll(disabled.filter { it.matches(availableSearchText) })
            enabled.removeAll(disabled)
        }
    }

    fun searchInAvailable(view: PluginsEditorView) {
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
        searchInAvailable(view)
    }

    private fun Plugin.matches(searchText: String): Boolean {
        if (type !in types) return false
        return name.matches(searchText) || author.matches(searchText) || id.matches(searchText)
    }

    private fun String.matches(searchText: String) = startsWith(searchText)

    companion object {
        private const val LIMIT = 20
    }
}