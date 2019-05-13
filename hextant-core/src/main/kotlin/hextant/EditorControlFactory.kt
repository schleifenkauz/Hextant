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
     * From now all calls of [getControl] with an argument of type [E] will use the [viewFactory]
     */
    fun <E : Editor<*>> register(editableCls: KClass<out E>, viewFactory: (E, Bundle) -> EditorControl<*>)

    /**
     * @return the [EditorControl<*>] associated with the type of the specified [editor]
     * @throws NoSuchElementException if there is no [EditorControl<*>] registered with this [editor]
     */
    fun <E : Editor<*>> getControl(
        editor: E,
        arguments: Bundle = Bundle.newInstance()
    ): EditorControl<*>

    @Suppress("UNCHECKED_CAST") private class Impl : EditorControlFactory {
        private val viewFactories = ClassMap.invariant<(Editor<*>, Bundle) -> EditorControl<*>>()

        override fun <E : Editor<*>> register(
            editableCls: KClass<out E>,
            viewFactory: (E, Bundle) -> EditorControl<*>
        ) {
            viewFactories[editableCls] = viewFactory as (Editor<*>, Bundle) -> EditorControl<*>
        }

        @Synchronized override fun <E : Editor<*>> getControl(
            editor: E,
            arguments: Bundle
        ): EditorControl<*> {
            val cls = editor::class
            viewFactories[cls]?.let { f -> return f(editor, arguments) }
            unresolvedView(cls)
        }

        private fun <E : Editor<*>> unresolvedView(cls: KClass<out E>): Nothing {
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

