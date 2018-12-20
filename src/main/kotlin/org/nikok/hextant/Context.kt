/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.bundle.Bundle

interface Context : Bundle {
    val platform: HextantPlatform

    val parent: Context?
}