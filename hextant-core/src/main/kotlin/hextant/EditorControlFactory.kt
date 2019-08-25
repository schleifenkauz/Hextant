/**
 *@author Nikolaus Knop
 */

package hextant

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.impl.myLogger
import hextant.util.ClassMap
import kotlin.reflect.KClass

/**
 * Used to manage the views of [Editor]s
 */
interface EditorControlFactory {
    /**
     * Register the specified [viewFactory] to the given [editableCls].
     * From now all calls of [createControl] with an argument of type [E] will use the [viewFactory]
     */
    fun <E : Editor<*>> register(editableCls: KClass<out E>, viewFactory: (E, Bundle) -> EditorControl<*>)

    /**
     * @return the [EditorControl<*>] associated with the type of the specified [editor] or null if there is no registered factory
     */
    fun <E : Editor<*>> createControl(
        editor: E,
        arguments: Bundle = Bundle.newInstance()
    ): EditorControl<*>?

    @Suppress("UNCHECKED_CAST") private class Impl : EditorControlFactory {
        private val viewFactories = ClassMap.invariant<(Editor<*>, Bundle) -> EditorControl<*>>()

        override fun <E : Editor<*>> register(
            editableCls: KClass<out E>,
            viewFactory: (E, Bundle) -> EditorControl<*>
        ) {
            viewFactories[editableCls] = viewFactory as (Editor<*>, Bundle) -> EditorControl<*>
        }

        @Synchronized override fun <E : Editor<*>> createControl(
            editor: E,
            arguments: Bundle
        ): EditorControl<*>? {
            val cls = editor::class
            val factory = viewFactories[cls] ?: return null
            return factory(editor, arguments)
        }
    }

    companion object : Property<EditorControlFactory, Public, Internal>("editor-view-factory") {
        /**
         * Creates a new [EditorControlFactory]
         */
        fun newInstance(): EditorControlFactory =
            Impl()

        /**
         * Logger used by all instances of [EditorControlFactory]
         */
        val logger by myLogger()
    }
}

