/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.editable.Expandable

open class ConfiguredExpander<E : Editable<*>, Ex: ConfiguredExpander<E, Ex>>(
    private val config: ExpanderConfig<E, Ex>,
    edited: Expandable<*, E>,
    platform: HextantPlatform
): Expander<E>(edited, platform) {
    @Suppress("UNCHECKED_CAST")
    override fun expand(text: String): E? = config.expand(text, this as Ex)
}