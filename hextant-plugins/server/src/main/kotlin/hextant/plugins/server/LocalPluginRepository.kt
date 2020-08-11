/**
 *@author Nikolaus Knop
 */

package hextant.plugins.server

import hextant.plugins.*
import hextant.plugins.Plugin.Type
import kollektion.Trie
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parse
import java.io.File
import java.util.*
import java.util.jar.JarFile

@Suppress("BlockingMethodInNonBlockingContext")
class LocalPluginRepository(private val root: File) : Marketplace {
    private val trie = Trie<Plugin>()
    private val implementations = mutableMapOf<String, MutableMap<String, ImplementationBundle>>()

    init {
        preprocess()
    }

    @OptIn(ImplicitReflectionSerializer::class)
    private inline fun <reified T : Any> JarFile.getInfo(file: String): T? {
        val entry = getEntry(file) ?: return null
        val reader = getInputStream(entry).bufferedReader()
        return json.parse(reader.readText())
    }

    private fun preprocess() {
        for (item in root.listFiles()!!) {
            if (item.extension != "jar") continue
            val jar = JarFile(item)
            val plugin: Plugin? = jar.getInfo("plugin.json")
            val impl: ImplementationBundle? = jar.getInfo("implementation.json")
            if (plugin != null) addPlugin(plugin)
            if (impl != null) addImplementation(impl)
        }
    }

    private fun addImplementation(bundle: ImplementationBundle) {
        for (impl in bundle.implementations) {
            val impls = implementations.getOrPut(impl.aspect) { mutableMapOf() }
            impls[impl.case] = bundle
        }
    }

    private fun addPlugin(plugin: Plugin) {
        trie.insert(plugin.author, plugin)
        trie.insert(plugin.id, plugin)
        trie.insert(plugin.name, plugin)
        for (type in plugin.projectTypes) trie.insert(type.name, plugin)
    }

    @OptIn(ImplicitReflectionSerializer::class)
    override suspend fun getPluginById(id: String): Plugin? {
        val file = download(id) ?: return null
        val jar = JarFile(file)
        val desc = jar.getEntry("plugin.json") ?: return null
        val reader = jar.getInputStream(desc).bufferedReader()
        return json.parse(reader.readText())
    }

    override suspend fun getPlugins(
        searchText: String,
        limit: Int,
        types: Set<Type>,
        excluded: Set<String>
    ): List<Plugin> {
        val node = trie.getNode(searchText) ?: return emptyList()
        val q: Queue<Trie.Node<Plugin>> = LinkedList()
        q.offer(node)
        val result = mutableSetOf<Plugin>()
        while (q.isNotEmpty() && result.size < limit) {
            val n = q.poll()
            for (v in n.values) {
                if (v.type in types && v.id !in excluded) result.add(v)
            }
            for (c in n.children.values) q.offer(c)
        }
        return result.toList()
    }

    override suspend fun getImplementation(aspect: String, case: String): ImplementationBundle? =
        implementations[aspect]?.get(case)

    override suspend fun download(id: String): File? {
        val file = root.resolve("$id.jar")
        if (!file.exists()) return null
        return file
    }

    companion object {
        private val json = Json(JsonConfiguration.Stable)
    }
}