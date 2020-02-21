/**
 *@author Nikolaus Knop
 */

package hextant.bundle

/**
 * Indicates the change of [property] from [oldValue] to [newValue].
 * @property bundle the [Bundle] which changed
 * @property property the property that changed
 * @property oldValue the old value of the property
 * @property newValue the new value of the property
 */
data class BundleChange<T : Any>(
    val bundle: Bundle,
    val property: ReactiveProperty<in T, *, *>,
    val oldValue: T?,
    val newValue: T
)