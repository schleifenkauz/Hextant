/**
 *@author Nikolaus Knop
 */

package hextant.plugins.server

import hextant.plugins.*
import hextant.plugins.Plugin.Type
import kollektion.Trie
import java.io.File
import java.util.*
import java.util.jar.JarFile
import kotlin.collections.set

class LocalPluginRepository(private val root: File) : Marketplace {
    private val trie = Trie<Plugin>()
    private val implementations = mutableMapOf<String, MutableMap<String, ImplementationCoord>>()

    init {
        preprocess()
    }

    private fun preprocess() {
        for (item in root.listFiles()!!) {
            if (item.extension != "jar") continue
            val jar = JarFile(item)
            val plugin: Plugin? = jar.getInfo("plugin.json")
            val impl: List<Implementation>? = jar.getInfo("implementations.json")
            if (plugin != null) addPlugin(plugin)
            if (impl != null) {
                val bundle = item.nameWithoutExtension.takeIf { plugin == null }
                addImplementations(impl, bundle)
            }
        }
    }

    private fun addImplementations(impls: List<Implementation>, bundle: String?) {
        for (impl in impls) {
            val forAspect = implementations.getOrPut(impl.aspect) { mutableMapOf() }
            forAspect[impl.feature] = ImplementationCoord(bundle, impl.clazz)
        }
    }

    private fun addPlugin(plugin: Plugin) {
        trie.insert(plugin.author, plugin)
        trie.insert(plugin.id, plugin)
        trie.insert(plugin.name, plugin)
    }

    override fun getPlugins(
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

    override fun getImplementation(aspect: String, feature: String): ImplementationCoord? =
        implementations[aspect]?.get(feature)

    override fun getJarFile(id: String): File? {
        val file = root.resolve("$id.jar")
        if (!file.exists()) return null
        return file
    }
}