/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import org.nikok.hextant.HextantPlatform

abstract class DefaultRegistration(private val doRegister: HextantPlatform.() -> Unit) {
    private val registeredPlatforms = mutableSetOf<HextantPlatform>()

    fun registerDefault(platform: HextantPlatform) {
        if (!registeredPlatforms.add(platform)) return
        platform.doRegister()
    }
}