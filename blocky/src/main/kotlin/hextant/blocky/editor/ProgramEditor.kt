/**
 *@author Nikolaus Knop
 */

package hextant.blocky.editor

import hextant.*
import hextant.base.AbstractEditor
import hextant.blocky.Program
import hextant.blocky.view.ProgramEditorView

class ProgramEditor(context: Context, initialBlock: BlockEditor) :
    AbstractEditor<Program, ProgramEditorView>(context) {
    private val start = initialBlock.moveTo(context)

    constructor(context: Context) : this(context, BlockEditor(context))

    private val components = mutableListOf<Editor<*>>()

    init {
        addComponent(start)
    }

    fun addComponent(comp: Editor<*>) {
        components.add(comp)
        children(*components.toTypedArray())
        views {
            addedComponent(components.size - 1, comp)
        }
    }

    fun removeComponent(comp: Editor<*>) {
        val idx = components.indexOf(comp)
        if (idx == -1) error("Component $comp not present")
        components.removeAt(idx)
        children(*components.toTypedArray())
        views {
            removedComponent(idx)
        }
    }

    override fun viewAdded(view: ProgramEditorView) {
        components.forEachIndexed { idx, comp -> view.addedComponent(idx, comp) }
    }

    override val result: EditorResult<Program> = result1(start) { s -> ok(Program(s)) }
}