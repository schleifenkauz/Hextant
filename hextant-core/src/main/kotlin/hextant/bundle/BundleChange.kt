/**
 *@author Nikolaus Knop
 */

package hextant.bundle

data class BundleChange<T>(
    val bundle: ReactiveBundle,
    val property: Property<T, *, *>,
    val newValue: T
)