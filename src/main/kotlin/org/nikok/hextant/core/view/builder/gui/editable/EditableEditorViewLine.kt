/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder.gui.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.core.list.EditableList
import org.nikok.hextant.core.view.builder.gui.EditorViewPart
import org.nikok.reaktive.value.*

class EditableEditorViewLine : Editable<List<EditorViewPart?>> {
    private val editableList = EditableList<EditorViewPart, EditableEditorViewPart>()

    override val edited by lazy { parts() }

    private fun parts(): ReactiveValue<List<EditorViewPart?>> =
        editableList.edited.map("edited of $this") { it.map { part -> part?.edited?.now } }

    override val isOk: ReactiveBoolean
        get() = editableList.isOk
}