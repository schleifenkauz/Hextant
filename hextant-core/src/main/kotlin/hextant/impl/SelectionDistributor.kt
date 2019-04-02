/**
 *@author Nikolaus Knop
 */

package hextant.impl

import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import reaktive.set.*
import reaktive.value.Variable

interface SelectionDistributor {
    val selectedEditors: ReactiveSet<Editor<*>>

    fun toggleSelection(editor: Editor<*>, isSelected: Variable<Boolean>)

    fun select(editor: Editor<*>, isSelected: Variable<Boolean>)

    private class Impl: SelectionDistributor {
        private val isEditorSelected: MutableMap<Editor<*>, Variable<Boolean>> = HashMap()

        override val selectedEditors: MutableReactiveSet<Editor<*>> = reactiveSet()

        @Synchronized override fun toggleSelection(editor: Editor<*>, isSelected: Variable<Boolean>) {
            val now = selectedEditors.now
            if (addSelection(editor, isSelected)) return
            if (now.size > 1) removeSelection(now, editor, isSelected)
        }

        private fun removeSelection(
            now: MutableSet<Editor<*>>, editor: Editor<*>, isSelected: Variable<Boolean>
        ) {
            if (now.remove(editor)) {
                isSelected.set(false)
                isEditorSelected.remove(editor)
            }
        }

        @Synchronized override fun select(editor: Editor<*>, isSelected: Variable<Boolean>) {
            println("Selecting $editor on $this")
            val now = selectedEditors.now
            val alreadySelected = editor in now
            if (now.size == 1 && alreadySelected) return
            else if (alreadySelected) {
                removeAllExcept(editor, isSelected)
            }
            else {
                clearSelection()
                addSelection(editor, isSelected)
            }
        }

        private fun clearSelection() {
            selectedEditors.now.forEach { isEditorSelected.remove(it)?.set(false) }
            selectedEditors.now.clear()
        }

        private fun addSelection(
            editor: Editor<*>, isSelected: Variable<Boolean>
        ): Boolean {
            val couldAdd = selectedEditors.now.add(editor)
            if (couldAdd) {
                isEditorSelected[editor] = isSelected
                isSelected.set(true)
            }
            return couldAdd
        }

        private fun removeAllExcept(
            editor: Editor<*>, isSelected: Variable<Boolean>
        ) {
            selectedEditors.now.removeIf {
                if (it != editor) {
                    isEditorSelected[editor] = isSelected
                    true
                } else false
            }
        }
    }

    companion object : Property<SelectionDistributor, Public, Public>("Selection Distributor") {
        fun newInstance(): SelectionDistributor = Impl()
    }
}