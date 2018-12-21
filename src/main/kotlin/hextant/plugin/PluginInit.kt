/**
 * @author Nikolaus Knop
 */

package hextant.plugin

import hextant.HextantPlatform

interface PluginInit {
    fun initialize(platform: HextantPlatform)
}