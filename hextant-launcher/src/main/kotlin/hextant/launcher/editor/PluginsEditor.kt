/**
 *@author Nikolaus Knop
 */

package hextant.launcher.editor

import hextant.context.Context
import hextant.core.editor.AbstractEditor
import hextant.launcher.HextantPlatform.marketplace
import hextant.launcher.plugins.PluginException
import hextant.launcher.plugins.PluginManager
import hextant.launcher.view.PluginsEditorView
import hextant.plugins.Plugin
import hextant.plugins.PluginInfo.Type
import kotlinx.coroutines.*
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue
import validated.valid

internal class PluginsEditor(
    context: Context,
    private val manager: PluginManager,
    private val types: Set<Type>
) : AbstractEditor<Collection<Plugin>, PluginsEditorView>(context) {
    override val result: ReactiveValue<Collection<Plugin>> get() = reactiveValue(valid(manager.enabledPlugins()))

    fun enable(plugin: Plugin, view: PluginsEditorView) {
        val activated = try {
            manager.enable(plugin, view::confirmEnable) ?: return
        } catch (e: PluginException) {
            return view.alertError(e.message!!)
        }
        views {
            launch {
                available.removeAll(activated)
                enabled.addAll(activated.filter { it.matches(enabledSearchText) })
            }
        }
    }

    private inline fun launch(crossinline action: suspend () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            action()
        }
    }

    fun disable(plugin: Plugin, view: PluginsEditorView) {
        val disabled = try {
            manager.disable(plugin, view::confirmDisable, view::askDisable) ?: return
        } catch (e: PluginException) {
            return view.alertError(e.message!!)
        }

        views {
            launch {
                available.addAll(disabled.filter { it.info.await().type in types && it.matches(availableSearchText) })
                enabled.removeAll(disabled)
            }
        }
    }

    fun searchInAvailable(view: PluginsEditorView) {
        launch {
            val marketplace = context[marketplace]
            val available = marketplace.getPlugins(view.availableSearchText, LIMIT, types, manager.enabledIds())
            view.available.clear()
            view.available.addAll(available.map { id -> manager.getPlugin(id) })
        }
    }

    fun searchInEnabled(view: PluginsEditorView) {
        launch {
            val enabled = manager.enabledPlugins().filter { it.matches(view.enabledSearchText) }
            view.enabled.clear()
            view.enabled.addAll(enabled)
        }
    }

    override fun viewAdded(view: PluginsEditorView) {
        searchInEnabled(view)
        searchInAvailable(view)
    }

    private suspend fun Plugin.matches(searchText: String): Boolean {
        return info.await().name.matches(searchText) || info.await().author.matches(searchText) || id.matches(searchText)
    }

    private fun String.matches(searchText: String) = startsWith(searchText)

    companion object {
        private const val LIMIT = 20
    }
}