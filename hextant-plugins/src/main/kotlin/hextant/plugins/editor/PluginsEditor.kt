/**
 *@author Nikolaus Knop
 */

package hextant.plugins.editor

import hextant.completion.CompletionResult.Match
import hextant.completion.CompletionStrategy
import hextant.context.Context
import hextant.core.editor.AbstractEditor
import hextant.plugins.Plugin
import hextant.plugins.client.PluginClient
import hextant.plugins.view.PluginsEditorView
import kotlinx.coroutines.*
import reaktive.Observer
import reaktive.asValue
import reaktive.set.reactiveSet
import reaktive.value.binding.map
import reaktive.value.forEach
import reaktive.value.now
import validated.valid
import java.util.*

class PluginsEditor(context: Context, private val client: PluginClient, private val types: Set<Plugin.Type>) :
    AbstractEditor<Collection<String>, PluginsEditorView>(context) {
    private val searchTextObservers = WeakHashMap<PluginsEditorView, Observer>()

    private val available = runBlocking { client.getAllPlugins() }.toMutableSet()

    private val enabled = reactiveSet<String>()

    override val result = enabled.asValue().map { valid(it.now) }

    fun enable(id: String) {
        check(available.remove(id))
        check(enabled.now.add(id))
        GlobalScope.launch { client.getJarFile(id) }
        views {
            enabled(id)
            if (isAvailable(id, searchText.now)) notAvailable(id)
        }
    }

    fun disable(id: String) {
        check(enabled.now.remove(id))
        check(available.add(id))
        views {
            if (isAvailable(id, searchText.now)) available(id)
            disabled(id)
        }
    }

    fun getInfo(id: String): Plugin = runBlocking { client.getById(id) }

    override fun viewAdded(view: PluginsEditorView) {
        val observer = view.searchText.forEach { text ->
            val filtered = available.filter { id -> isAvailable(id, text) }
            view.showAvailable(filtered)
        }
        searchTextObservers[view] = observer
    }

    private fun isAvailable(id: String, searchText: String): Boolean {
        val plugin = getInfo(id)
        if (plugin.type !in types) return false
        return matches(searchText, plugin.name) || matches(searchText, plugin.author)
    }

    private fun matches(searchText: String, name: String) =
        CompletionStrategy.simple.match(searchText, name) is Match
}