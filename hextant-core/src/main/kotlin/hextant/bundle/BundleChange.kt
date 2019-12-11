/**
 *@author Nikolaus Knop
 */

package hextant.bundle

/**
 * Represents a change in a [Bundle]
 * @property bundle the [Bundle] that changed
 * @property property the property whose value changed
 * @property newValue the new value of the property
 */
data class BundleChange<T : Any>(
    val bundle: ReactiveBundle,
    val property: Property<in T, *, *>,
    val newValue: T
)