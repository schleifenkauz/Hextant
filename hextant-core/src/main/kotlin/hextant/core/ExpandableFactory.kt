/**
 * @author Nikolaus Knop
 */

package hextant.core

import hextant.Editable
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.editable.Expandable
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
     * Create an expandable for the given [editable] using the previously registered factory for any superclass of [E]
     * @throws NoSuchElementException if no factory for this type of editable was registered
     */
    fun <E : Editable<*>> createExpandable(editable: E): Expandable<*, E>

    companion object : Property<EditableFactory, Public, Internal>("Expandable Factory") {
        fun newInstance(): ExpandableFactory = TODO()
    }
}