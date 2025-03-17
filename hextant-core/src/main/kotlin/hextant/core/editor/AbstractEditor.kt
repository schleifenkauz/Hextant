/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.context.executeSafely
import hextant.core.Editor
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

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

    private val children = mutableListOf<Editor<*>>()

    val viewManager: ListenerManager<@UnsafeVariance V> = ListenerManager.createWeakListenerManager()

    override fun getChildren(): Collection<Editor<*>> = children

    final override fun initialize(context: Context) {
        if (isInitialized) throw IllegalStateException("Already initialized")
        this.context = context
        for (child in getChildren()) {
            child.initialize(context)
        }
        doInitialize()
        isInitialized = true
    }

    protected open fun doInitialize() {}

    override fun locate(parent: Editor<*>?, accessor: EditorAccessor, expander: Expander<*, *>?) {
        this.parent = parent
        this.accessor = accessor
        this.expander = expander
    }

    override fun implCopy(): Editor<R> {
        val serializer = serializer<Editor<R>>()
        val json = Json.encodeToString(serializer, this)
        return Json.decodeFromString(serializer, json)
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        throw InvalidAccessorException(accessor)
    }

    override fun paste(editor: Editor<*>): Boolean = false

    /**
     * Makes the [editor] a child of this editor and just returns it
     */
    protected fun <E : Editor<*>> addChild(editor: E): E {
        children.add(editor)
        return editor
    }

    /**
     * Removes the given [editor] from the [children].
     * @throws IllegalStateException if [editor] is not a child of this editor.
     */
    protected fun <E : Editor<*>> removeChild(editor: E) {
        if (!children.remove(editor)) throw IllegalStateException("$editor is not a child of $this")
    }

    /**
     * Make all the given editors children of this editor
     */
    protected fun children(vararg children: Editor<*>) {
        for (c in children) addChild(c)
    }

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
}