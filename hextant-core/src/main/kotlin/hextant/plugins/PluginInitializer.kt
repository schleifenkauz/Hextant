/**
 *@author Nikolaus Knop
 */

package hextant.plugins

import hextant.context.Context
import hextant.core.Editor

/**
 * Subclasses of this class are used to initialize plugins.
 */
abstract class PluginInitializer(private val initialize: PluginBuilder.() -> Unit) {
    /**
     * Applies this plugin to the the given [context] wrapping in a [PluginBuilder].
     */
    fun apply(context: Context, phase: PluginBuilder.Phase, project: Editor<*>?, testing: Boolean = false) {
        val builder = PluginBuilder(phase, context, project, testing)
        builder.initialize()
    }
}