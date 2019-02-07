/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.bundle.Bundle

interface Context : Bundle {
    val platform: HextantPlatform

    val parent: Context?

    companion object {
        inline fun newInstance(
            platform: HextantPlatform = HextantPlatform.configured(),
            configure: Bundle.() -> Unit = {}
        ): Context = object : AbstractContext(platform) {
            override val platform: HextantPlatform
                get() = platform
        }.apply(configure)
    }
}