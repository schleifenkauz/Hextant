/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.blocky.Program
import hextant.blocky.view.ProgramEditorView

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

    override val result: EditorResult<Program> = result1(entry) { s -> ok(Program(s)) }
}