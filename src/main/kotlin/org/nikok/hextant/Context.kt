/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.bundle.Bundle
import javax.xml.stream.XMLStreamReader

interface Context : Bundle {
    val platform: HextantPlatform

    val parent: Context?

    fun loadPlugin(xml: XMLStreamReader)
}