package hextant.core.editor

interface ViewManager<V : Any> {
    /**
     * @return a sequence of all views registered to this editor
     */
    val views: Sequence<V>

    /**
     * Execute the given [action] on all views
     */
    fun notifyViews(action: (V).() -> Unit)

    /**
     * Add the specified [view] to this editor, such that it will be notified when the editor is modified
     * * eventually the editor will directly call methods of the view
     * so be careful when adding a view in the constructor
     * * Adding a view to an editor will not prevent the view from being garbage collected
     */
    fun addView(view: V)

    companion object {
        fun <V : Any> createWeakViewManager(): ViewManager<V> = WeakViewManager()
    }

    fun removeView(view: V)
}