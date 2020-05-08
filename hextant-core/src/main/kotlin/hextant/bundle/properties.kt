/**
 * @author Nikolaus Knop
 */

package hextant.bundle

open class Property<T, in Read : Any, in Write : Any>(
    internal val name: String? = null,
    private val default: T? = null
) {
    internal fun default() = default

    override fun toString(): String = name ?: javaClass.name
}

open class ReactiveProperty<T, Read : Any, Write : Any>(name: String? = null, default: T? = null) :
    Property<T, Read, Write>(name, default)

open class SimpleProperty<T>(name: String? = null, default: T? = null) : Property<T, Any, Any>(name, default)

open class SimpleReactiveProperty<T>(name: String? = null, default: T? = null) :
    ReactiveProperty<T, Any, Any>(name, default)