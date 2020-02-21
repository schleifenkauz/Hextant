/**
 * @author Nikolaus Knop
 */

package hextant.plugin

import hextant.Context

interface PluginExit {
    fun exit(platform: Context)
}