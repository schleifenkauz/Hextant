/**
 *@author Nikolaus Knop
 */

package hextant.core.view

import bundles.Bundle
import bundles.Property
import hextant.Editor
import hextant.fx.EditorControl

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

    override fun argumentChanged(property: Property<*, *, *>, value: Any?) {
        view.argumentChanged(property, value)
    }

    override fun setEditorParent(parent: EditorControl<*>) {
        view.setEditorParent(parent)
    }

    override fun setNext(nxt: EditorControl<*>) {
        view.setNext(nxt)
    }

    override fun setPrevious(prev: EditorControl<*>) {
        view.setPrevious(prev)
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

    override fun deselect() {
        view.deselect()
    }

    override fun createDefaultRoot(): EditorControl<*> = view
}