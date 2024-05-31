package hextant.core.editor

import java.lang.ref.WeakReference

internal class WeakListenerManager<V: Any> : ListenerManager<V> {
    private val mutableViews = mutableListOf<WeakReference<V>>()
    /**
     * @return a sequence of all views registered to this editor
     */
    override val listeners: Sequence<@UnsafeVariance V>
        get() {
            val itr = mutableViews.iterator()
            tailrec fun next(): V? =
                if (!itr.hasNext()) null
                else {
                    val nxt = itr.next()
                    if (nxt.get() != null) nxt.get()
                    else {
                        itr.remove()
                        next()
                    }
                }
            return generateSequence { next() }
        }

    /**
     * Execute the given [action] on all views
     */
    override fun notifyListeners(action: (@UnsafeVariance V).() -> Unit) {
        try {
            listeners.forEach(action)
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
        if (listener in listeners) {
            throw IllegalArgumentException("View already added: $listener")
        }
        mutableViews.add(WeakReference(listener))
    }

    override fun removeListener(view: V) {
        mutableViews.removeIf { ref -> ref.get() == view }
    }
}