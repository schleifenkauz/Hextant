/**
 *@author Nikolaus Knop
 */

package hextant.context

import bundles.*
import hextant.core.Editor
import hextant.core.view.EditorControl
import kollektion.ClassMap
import kotlin.reflect.KClass

/**
 * Used to manage the views of [Editor]s
 */
class EditorControlFactory private constructor() {
    private val viewFactories = ClassMap.contravariant<(Editor<*>, Bundle) -> EditorControl<*>>()

    /**
     * Register the specified [viewFactory] to the given [editableCls].
     * From now all calls of [createControl] with an argument of type [E] will use the [viewFactory]
     */
    fun <E : Editor<*>> register(editableCls: KClass<out E>, viewFactory: (E, Bundle) -> EditorControl<*>) {
        @Suppress("UNCHECKED_CAST")
        viewFactories[editableCls] = viewFactory as (Editor<*>, Bundle) -> EditorControl<*>
    }

    /**
     * @return the [EditorControl<*>] associated with the type of the specified [editor] or null if there is no registered factory
     */
    fun <E : Editor<*>> createControl(
        editor: E,
        arguments: Bundle = createBundle()
    ): EditorControl<*>? {
        val cls = editor::class
        val factory = viewFactories[cls] ?: return null
        return factory(editor, arguments)
    }

    companion object : Property<EditorControlFactory, Any, Internal>("editor-view-factory") {
        /**
         * Creates a new [EditorControlFactory]
         */
        fun newInstance(): EditorControlFactory =
            EditorControlFactory()
    }
}

