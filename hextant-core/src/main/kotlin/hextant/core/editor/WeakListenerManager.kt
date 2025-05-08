package hextant.core.editor

import java.lang.ref.WeakReference

internal class WeakListenerManager<V: Any> : ListenerManager<V> {
    /**
     * @return a sequence of all views registered to this editor
     */
    private val listeners = mutableListOf<WeakReference<V>>()

    override fun listeners() = listeners.mapNotNull { ref -> ref.get() }

    /**
     * Execute the given [action] on all views
     */
    override fun notifyListeners(action: (@UnsafeVariance V).() -> Unit) {
        try {
            listeners.removeIf { ref -> ref.get() == null }
            listeners.mapNotNull { ref -> ref.get() }.forEach(action)
        } catch (e: Throwable) {
            println("Exception while updating views")
            e.printStackTrace()
        }
    }

    /**
     * Add the specified [listener] to this editor, such that it will be notified when the editor is modified
     * * eventually the editor will directly call methods of the view
     * so be careful when adding a view in the constructor
     * * Adding a view to an editor will not prevent the view from being garbage collected
     */
    override fun addListener(listener: V) {
        if (listeners.any { ref -> ref.get() == listener }) {
            throw IllegalArgumentException("View already added: $listener")
        }
        listeners.add(WeakReference(listener))
    }

    override fun removeListener(view: V) {
        listeners.removeIf { ref -> ref.get() == view }
    }
}