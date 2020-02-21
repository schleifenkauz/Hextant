/**
 * @author Nikolaus Knop
 */

package hextant.bundle

fun createBundle(): Bundle = BundleImpl()

inline fun createBundle(configure: Bundle.() -> Unit) = createBundle().apply(configure)