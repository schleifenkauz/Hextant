/**
 *@author Nikolaus Knop
 */

package hextant.plugins

import hextant.plugins.PluginInfo.Type
import kollektion.Trie
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.jar.JarFile
import kotlin.collections.set
import kotlin.reflect.full.isSubclassOf

class LocalPluginRepository(private val root: File) : Marketplace {
    private val trie = Trie<PluginInfo>()
    private val implementations = mutableMapOf<String, MutableMap<String, ImplementationCoord>>()
    private val projectTypes = mutableMapOf<String, LocatedProjectType>()

    init {
        preprocess()
    }

    private fun preprocess() {
        for (item in root.listFiles()!!) {
            uploaded(item)
        }
    }

    private fun uploaded(item: File) {
        if (item.extension != "jar") return
        val id = item.nameWithoutExtension
        JarFile(item).use { jar ->
            val info: PluginInfo? = jar.getInfo("plugin.json")
            val impl: List<Implementation>? = jar.getInfo("implementations.json")
            val pts: List<ProjectType>? = jar.getInfo("projectTypes.json")
            if (info != null) addPlugin(info)
            if (impl != null) {
                val bundle = id.takeIf { info == null }
                addImplementations(impl, bundle)
            }
            if (pts != null) addProjectTypes(pts, id)
        }
    }

    private fun addProjectTypes(pts: List<ProjectType>, id: String) {
        for ((name, clazz) in pts) {
            projectTypes[name] = LocatedProjectType(name, clazz, id)
        }
    }

    private fun addImplementations(impls: List<Implementation>, bundle: String?) {
        for (impl in impls) {
            val forAspect = implementations.getOrPut(impl.aspect) { mutableMapOf() }
            forAspect[impl.feature] = ImplementationCoord(bundle, impl.clazz)
        }
    }

    private fun addPlugin(info: PluginInfo) {
        trie.insert(info.author, info)
        trie.insert(info.id, info)
        trie.insert(info.name, info)
    }

    override suspend fun getPlugins(
        searchText: String,
        limit: Int,
        types: Set<Type>,
        excluded: Set<String>
    ): List<String> {
        val node = trie.getNode(searchText) ?: return emptyList()
        val q: Queue<Trie.Node<PluginInfo>> = LinkedList()
        q.offer(node)
        val result = mutableSetOf<String>()
        while (q.isNotEmpty() && result.size < limit) {
            val n = q.poll()
            for (v in n.values) {
                if (v.type in types && v.id !in excluded) result.add(v.id)
            }
            for (c in n.children.values) q.offer(c)
        }
        return result.toList()
    }

    override suspend fun <T : Any> get(property: PluginProperty<T>, pluginId: String): T? = getInfo(property, pluginId)

    override suspend fun getImplementation(aspect: String, feature: String): ImplementationCoord? =
        implementations[aspect]?.get(feature)

    override suspend fun availableProjectTypes(): List<LocatedProjectType> = projectTypes.values.toList()

    override suspend fun getProjectType(name: String): LocatedProjectType? =
        projectTypes[name] ?: anonymousProjectType(name)

    private fun anonymousProjectType(className: String): LocatedProjectType? = try {
        val clazz = Class.forName(className).kotlin
        if (clazz.isSubclassOf(CLS_PROJECT_TYPE))
            LocatedProjectType("<anonymous>", className, "<anonymous>")
        else null
    } catch (e: ClassNotFoundException) {
        null
    }

    override suspend fun getJarFile(id: String): File? {
        val file = root.resolve("$id.jar")
        if (!file.exists()) return null
        return file
    }

    override suspend fun upload(jar: File) {
        uploaded(jar)
    }

    fun upload(file: InputStream, name: String) {
        val dest = File(root, name)
        file.copyTo(dest.outputStream())
        uploaded(dest)
    }

    companion object {
        private val CLS_PROJECT_TYPE = Class.forName("hextant.project.ProjectType").kotlin
    }
}