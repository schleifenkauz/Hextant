/**
 * @author Nikolaus Knop
 */

package hextant.plugin.dsl

import hextant.HextantPlatform
import hextant.bundle.CorePermissions.Public
import hextant.bundle.CoreProperties.classLoader
import hextant.plugin.Plugin
import hextant.plugin.PluginException
import java.io.Reader
import javax.script.ScriptEngineManager

class DslPluginLoader(private val platform: HextantPlatform) {
    private val engine by lazy { ScriptEngineManager(platform[Public, classLoader]).getEngineByExtension("kts") }

    fun loadPlugin(reader: Reader): Plugin {
        return engine.eval(reader) as? Plugin
            ?: throw PluginException(null, "Plugin script didn't return a plugin")
    }
}