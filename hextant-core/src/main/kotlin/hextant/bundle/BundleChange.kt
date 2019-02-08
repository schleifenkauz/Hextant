/**
 *@author Nikolaus Knop
 */

package hextant.bundle

data class BundleChange<T : Any>(
    val bundle: ReactiveBundle,
    val property: Property<in T, *, *>,
    val newValue: T
)