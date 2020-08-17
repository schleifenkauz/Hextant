/**
 *@author Nikolaus Knop
 */

package hextant.plugin

import hextant.context.Context

/**
 * Subclasses of this class are used to initialize plugins.
 */
abstract class PluginInitializer(private val initialize: PluginBuilder.() -> Unit) {
    /**
     * Applies this plugin to the the given [context] wrapping in a [PluginBuilder].
     */
    fun apply(context: Context) {
        val builder = PluginBuilder(context)
        builder.initialize()
    }
}