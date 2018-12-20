/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core

import org.nikok.hextant.Context
import org.nikok.hextant.Editable
import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.base.EditorControl
import org.nikok.hextant.core.editable.ConvertedEditable
import org.nikok.hextant.core.impl.ClassMap
import org.nikok.hextant.core.impl.myLogger
import kotlin.reflect.KClass

/**
 * Used to manage the views of [Editable]s
 */
interface EditorControlFactory {
    /**
     * Register the specified [viewFactory] to the given [editableCls].
     * From now all calls of [getControl] with an argument of type [E] will use the [viewFactory]
     */
    fun <E : Editable<*>> register(editableCls: KClass<out E>, viewFactory: (E, Context) -> EditorControl<*>)

    /**
     * @return the [EditorControl<*>] associated with the type of the specified [editable]
     * @throws NoSuchElementException if there is no [EditorControl<*>] registered with this [editable]
     */
    fun <E : Editable<*>> getControl(editable: E, context: Context): EditorControl<*>

    @Suppress("UNCHECKED_CAST") private class Impl : EditorControlFactory {
        private val viewFactories = ClassMap.invariant<(Editable<*>, Context) -> EditorControl<*>>()

        override fun <E : Editable<*>> register(
            editableCls: KClass<out E>,
            viewFactory: (E, Context) -> EditorControl<*>
        ) {
            viewFactories[editableCls] = viewFactory as (Editable<*>, Context) -> EditorControl<*>
        }

        override fun <E : Editable<*>> getControl(editable: E, context: Context): EditorControl<*> {
            val cls = editable::class
            when (editable) {
                is ConvertedEditable<*, *> -> return getControl(editable.source, context)
                else                       -> {
                    viewFactories[cls]?.let { f -> return f(editable, context) }
                    unresolvedView(cls)
                }
            }
        }

        private fun <E : Editable<*>> unresolvedView(cls: KClass<out E>): Nothing {
            val msg = "Could not resolve view for $cls"
            throw NoSuchElementException(msg)
        }

    }

    companion object : Property<EditorControlFactory, Public, Internal>("editor-view-factory") {
        fun newInstance(): EditorControlFactory =
            Impl()

        val logger by myLogger()
    }
}

inline fun <reified E : Editable<*>> EditorControlFactory.register(
    noinline viewFactory: (E, Context) -> EditorControl<*>
) {
    register(E::class, viewFactory)
}

inline fun EditorControlFactory.configure(config: EditorControlFactory.() -> Unit) {
    apply(config)
}