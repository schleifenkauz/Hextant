/**
 * @author Nikolaus Knop
 */

package hextant.plugin

import hextant.HextantPlatform

interface PluginExit {
    fun exit(platform: HextantPlatform)
}