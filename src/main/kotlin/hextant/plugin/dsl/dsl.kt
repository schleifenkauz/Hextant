/**
 * @author Nikolaus Knop
 */

package hextant.plugin.dsl

import hextant.HextantPlatform
import hextant.plugin.Plugin

inline fun plugin(crossinline block: PluginBuilder.() -> Unit): (HextantPlatform) -> Plugin {
    return { platform ->
        PluginBuilder(platform).apply(block).build()
    }
}