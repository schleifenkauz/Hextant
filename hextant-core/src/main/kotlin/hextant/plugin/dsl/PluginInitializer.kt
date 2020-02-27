/**
 *@author Nikolaus Knop
 */

package hextant.plugin.dsl

import hextant.Context
import hextant.plugin.Plugin

abstract class PluginInitializer(private val initialize: PluginBuilder.() -> Unit) {
    fun apply(context: Context): Plugin {
        val builder = PluginBuilder(context)
        builder.initialize()
        return builder.build()
    }
}