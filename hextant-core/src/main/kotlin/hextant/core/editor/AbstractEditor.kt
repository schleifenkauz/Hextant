/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Basic implementation for [Editor]s.
 */
abstract class AbstractEditor<out R, in V : Any> : Editor<R> {
    final override var isInitialized: Boolean = false
        private set

    final override lateinit var context: Context
        private set

    final override var parent: Editor<*>? = null
        private set

    final override lateinit var accessor: EditorAccessor
        private set

    final override var expander: Expander<*, *>? = null
        private set

    val viewManager: ListenerManager<@UnsafeVariance V> = ListenerManager.createWeakListenerManager()

    override fun getChildren(): Collection<Editor<*>> = emptyList()

    final override fun initialize(
        context: Context,
        parent: Editor<*>?,
        accessor: EditorAccessor,
        expander: Expander<*, *>?,
    ) {
        if (isInitialized) throw IllegalStateException("Already initialized")
        this.context = context
        this.parent = parent
        this.accessor = accessor
        this.expander = expander
        doInitialize()
        isInitialized = true
    }

    protected open fun doInitialize() {}

    override fun implCopy(): Editor<R> {
        val json = this.serialize(typeTag = true)
        @Suppress("UNCHECKED_CAST")
        val deserialized = Editor.deserializeWithTypeTag(json) as Editor<R>
        return deserialized
    }
    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        throw InvalidAccessorException(accessor)
    }

    override fun paste(editor: Editor<*>): Boolean = false

    fun notifyViews(action: (@UnsafeVariance V).() -> Unit) {
        viewManager.notifyListeners {
            context.executeSafely("notify views", Unit) {
                action()
            }
        }
    }

    protected open fun viewAdded(view: V) {}

    fun addView(view: V) {
        viewManager.addListener(view)
        viewAdded(view)
    }

    override fun serialize(typeTag: Boolean): JsonElement {
        val json = serialize()
        if (!typeTag) return json
        val type = JsonPrimitive(javaClass.canonicalName)
        if (json is JsonObject) {
            val typeTag = "_type" to type
            return JsonObject(json + typeTag)
        } else return buildJsonObject {
            put("_type", type)
            put("_content", json)
        }
    }

    override fun serialize(): JsonElement {
        throw UnsupportedOperationException("Cannot serialize $this")
    }

    override fun deserialize(element: JsonElement) {
        throw UnsupportedOperationException("Cannot serialize $this")
    }
}