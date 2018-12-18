/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.prop.PropertyHolder

interface Context : PropertyHolder {
    val platform: HextantPlatform

    private class Impl(
        private val holder: PropertyHolder,
        override val platform: HextantPlatform
    ) : Context, PropertyHolder by holder

    companion object {
        fun newInstance(platform: HextantPlatform): Context = Impl(PropertyHolder.newInstance(), platform)
    }
}