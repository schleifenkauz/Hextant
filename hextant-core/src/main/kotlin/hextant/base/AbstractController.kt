/**
 * @author Nikolaus Knop
 */

package hextant.base

import java.lang.ref.WeakReference

/**
 * Skeletal implementation for MVC-Controllers
 * * Note that this implementation does not store strong references to the registered views
 */
abstract class AbstractController<V : Any> {
    private val mutableViews = mutableSetOf<WeakReference<V>>()
    /**
     * @return a sequence of all views registered to this editor
     */
    protected val views: Sequence<V>
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
     * Executes the given action on the gui thread (whatever it is)
     * * Default implementation just invoke the [action] on the specified [view]
     */
    protected open fun onGuiThread(view: V, action: V.() -> Unit) {
        action(view)
    }

    /**
     * Execute the given [action] on all views [onGuiThread]
     */
    protected inline fun views(crossinline action: V.() -> Unit) {
        views.forEach { v -> onGuiThread(v) { action() } }
    }

    /**
     * Add the specified [view] to this editor, such that it will be notified when the editor is modified
     * * eventually the editor will directly call methods of the view
     * so be careful when adding a view in the constructor
     * * Adding a view to an editor will not prevent the view from being garbage collected
     */
    fun addView(view: V) {
        mutableViews.add(WeakReference(view))
        viewAdded(view)
    }

    /**
     * Is called when the specified [view] is added
     * * The default implementation does nothing
     */
    protected open fun viewAdded(view: V) {}
}