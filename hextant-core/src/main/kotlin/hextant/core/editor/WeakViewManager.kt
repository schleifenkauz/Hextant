package hextant.core.editor

import java.lang.ref.WeakReference

internal class WeakViewManager<V: Any> : ViewManager<V> {
    private val mutableViews = mutableListOf<WeakReference<V>>()
    /**
     * @return a sequence of all views registered to this editor
     */
    override val views: Sequence<@UnsafeVariance V>
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
    override fun notifyViews(action: (@UnsafeVariance V).() -> Unit) {
        try {
            views.forEach(action)
        } catch (e: Throwable) {
            println("Exception while updating views")
            e.printStackTrace()
        }
    }

    /**
     * Add the specified [view] to this editor, such that it will be notified when the editor is modified
     * * eventually the editor will directly call methods of the view
     * so be careful when adding a view in the constructor
     * * Adding a view to an editor will not prevent the view from being garbage collected
     */
    override fun addView(view: V) {
        if (view in views) {
            throw IllegalArgumentException("View already added: $view")
        }
        mutableViews.add(WeakReference(view))
    }
}