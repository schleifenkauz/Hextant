/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.plugin

import org.nikok.hextant.HextantPlatform

interface PluginExit {
    fun exit(platform: HextantPlatform)
}