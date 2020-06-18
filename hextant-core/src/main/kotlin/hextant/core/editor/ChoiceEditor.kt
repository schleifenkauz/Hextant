/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import hextant.context.Context
import hextant.core.Editor
import hextant.core.view.ChoiceEditorView
import reaktive.value.now
import reaktive.value.reactiveVariable
import validated.reaktive.ReactiveValidated
import validated.valid

/**
 * An [Editor] which supports choosing different items of type [C]
 */
abstract class ChoiceEditor<C : Any>(default: C, context: Context) : AbstractEditor<C, ChoiceEditorView<C>>(context) {
    private val selected = reactiveVariable(valid(default))

    override val result: ReactiveValidated<C>
        get() = selected

    /**
     * Select the given [choice]
     */
    fun select(choice: C) {
        selected.set(valid(choice))
        views { selected(choice) }
    }

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    private class Snapshot<C : Any>(original: ChoiceEditor<C>) : EditorSnapshot<ChoiceEditor<C>>(original) {
        private val selected = original.selected.now

        override fun reconstruct(editor: ChoiceEditor<C>) {
            editor.selected.now = selected
        }
    }
}