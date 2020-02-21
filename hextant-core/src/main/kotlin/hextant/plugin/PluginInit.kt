/**
 * @author Nikolaus Knop
 */

package hextant.plugin

import hextant.Context

interface PluginInit {
    fun initialize(platform: Context)
}