/**
 *@author Nikolaus Knop
 */

package hextant.plugin.dsl

import hextant.context.Context
import hextant.plugin.Plugin

/**
 * Subclasses of this class are used to initialize plugins.
 */
abstract class PluginInitializer(private val initialize: PluginBuilder.() -> Unit) {
    /**
     * Applies this plugin to the the given [context] wrapping in a [PluginBuilder].
     */
    fun apply(context: Context): Plugin {
        val builder = PluginBuilder(context)
        builder.initialize()
        return builder.build()
    }
}