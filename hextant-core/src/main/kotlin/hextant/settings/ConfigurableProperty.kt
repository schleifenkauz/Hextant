/**
 *@author Nikolaus Knop
 */

package hextant.settings

import bundles.PublicProperty
import hextant.context.EditorFactory

internal data class ConfigurableProperty<T : Any>(
    val property: PublicProperty<T>,
    val editorFactory: EditorFactory<T>
)
