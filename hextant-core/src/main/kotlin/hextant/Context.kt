/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.base.AbstractContext
import hextant.bundle.Bundle

interface Context : Bundle {
    val platform: HextantPlatform

    val parent: Context?

    companion object {
        inline fun newInstance(
            context: Context = HextantPlatform.configured(),
            configure: Context.() -> Unit = {}
        ): Context = object : AbstractContext(parent = context) {
            override val platform: HextantPlatform
                get() = context.platform
        }.apply(configure)
    }
}