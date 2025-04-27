/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.Property
import hextant.core.Editor

/**
 * Wraps the given [view]
 */
class EditorControlWrapper(
    editor: Editor<*>,
    private val view: EditorControl<*>,
    arguments: Bundle
) : EditorControl<EditorControl<*>>(editor, arguments) {
    init {
        setChildren(view)
    }

    override fun supportedParameters(): Collection<Property<*, *>> = view.supportedParameters()

    override fun <T : Any> argumentChanged(property: Property<T, *>, value: T) {
        view.argumentHandlers[property]?.forEach { handler -> handler.invoke(value) }
        view.argumentChanged(property, value)
    }

    override fun setEditorParent(parent: EditorControl<*>?) {
        view.setEditorParent(parent)
    }

    override fun focus() {
        view.focus()
    }

    override fun receiveFocus() {
        view.receiveFocus()
    }

    override fun requestFocus() {
        view.requestFocus()
    }

    override fun displaySelected(status: Boolean) {
        view.displaySelected(status)
    }

    override fun createDefaultRoot(): EditorControl<*> = view
}