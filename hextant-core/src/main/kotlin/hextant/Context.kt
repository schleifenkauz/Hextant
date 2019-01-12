/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.bundle.Bundle

interface Context : Bundle {
    val platform: HextantPlatform

    val parent: Context?
}