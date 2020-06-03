/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.base.EditorSnapshot
import hextant.blocky.Program
import hextant.blocky.view.ProgramEditorView
import validated.reaktive.ReactiveValidated
import validated.reaktive.composeReactive

class ProgramEditor(context: Context) :
    AbstractEditor<Program, ProgramEditorView>(context) {
    private val entry = EntryEditor(context)

    private val components = mutableListOf<Editor<*>>()

    init {
        addComponent(entry)
    }

    fun addComponent(comp: Editor<*>) {
        components.add(comp)
        views {
            addedComponent(components.size - 1, comp)
        }
    }

    fun removeComponent(comp: Editor<*>) {
        val idx = components.indexOf(comp)
        if (idx == -1) error("Component $comp not present")
        components.removeAt(idx)
        views {
            removedComponent(comp)
        }
    }

    override fun viewAdded(view: ProgramEditorView) {
        components.forEachIndexed { idx, comp -> view.addedComponent(idx, comp) }
    }

    override val result: ReactiveValidated<Program> = composeReactive(entry.result, ::Program)

    override fun createSnapshot(): EditorSnapshot<*> = Snapshot(this)

    private class Snapshot(original: ProgramEditor) : EditorSnapshot<ProgramEditor>(original) {
        private val entry = original.entry.snapshot()
        private val components = original.components.map { it.snapshot() }

        @Suppress("UNCHECKED_CAST")
        override fun reconstruct(editor: ProgramEditor) {
            entry as EditorSnapshot<Editor<*>>
            entry.reconstruct(editor.entry)
            editor.components.addAll(components.map { it.reconstruct(editor.context) })
        }
    }
}