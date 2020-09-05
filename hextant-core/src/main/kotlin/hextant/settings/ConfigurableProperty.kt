/**
 *@author Nikolaus Knop
 */

package hextant.settings

import bundles.Property
import hextant.context.EditorFactory

internal data class ConfigurableProperty<T : Any>(
    val property: Property<T, *, Any>,
    val editorFactory: EditorFactory<T>
)
