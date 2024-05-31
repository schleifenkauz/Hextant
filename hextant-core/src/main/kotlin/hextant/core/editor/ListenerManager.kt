package hextant.core.editor

interface ListenerManager<L : Any> {
    /**
     * @return a sequence of all views registered to this editor
     */
    val listeners: Sequence<L>

    /**
     * Execute the given [action] on all views
     */
    fun notifyListeners(action: (L).() -> Unit)

    /**
     * Add the specified [listener] to this editor, such that it will be notified when the editor is modified
     * * eventually the editor will directly call methods of the view
     * so be careful when adding a view in the constructor
     * * Adding a view to an editor will not prevent the view from being garbage collected
     */
    fun addListener(listener: L)

    fun removeListener(view: L)

    companion object {
        fun <V : Any> createWeakListenerManager(): ListenerManager<V> = WeakListenerManager()
    }
}