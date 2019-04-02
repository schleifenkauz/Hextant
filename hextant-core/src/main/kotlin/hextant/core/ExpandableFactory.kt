/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.Editable
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editable.Expandable
import hextant.util.ClassMap
import kotlin.reflect.KClass

/**
 * Used to register and resolve [Expandable]s for editors
 */
interface ExpandableFactory {
    /**
     * Register the the specified expandable-[factory] for the given class of editables
     */
    fun <E : Editable<*>> register(editableCls: KClass<E>, factory: () -> Expandable<*, E>)

    /**
     * Create an expandable for the given editable class using the previously registered factory for the class [E]
     * @throws NoSuchElementException if no factory for this type of editable was registered
     */
    fun <E : Editable<*>> createExpandable(cls: KClass<E>): Expandable<*, E>

    @Suppress("UNCHECKED_CAST")
    private class Impl : ExpandableFactory {
        private val factories = ClassMap.covariant<() -> Expandable<*, *>>()

        override fun <E : Editable<*>> register(editableCls: KClass<E>, factory: () -> Expandable<*, E>) {
            factories[editableCls] = factory
        }

        override fun <E : Editable<*>> createExpandable(cls: KClass<E>): Expandable<*, E> {
            val factory = factories[cls] ?: throw NoSuchElementException("No expandable registered for $cls")
            return factory() as Expandable<*, E>
        }
    }

    companion object : Property<ExpandableFactory, Public, Internal>("Expandable Factory") {
        fun newInstance(): ExpandableFactory = Impl()
    }
}