/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.plugin

import org.nikok.hextant.HextantPlatform

interface PluginInit {
    fun initialize(platform: HextantPlatform)
}