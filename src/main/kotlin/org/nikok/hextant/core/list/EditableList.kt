/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.list

import org.nikok.hextant.Editable
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.list.reactiveList
import org.nikok.reaktive.value.*
import org.nikok.reaktive.value.binding.binding

class EditableList<N, E : Editable<N>> : Editable<List<E?>> {
    val editableList = reactiveList<E>("editable list")

    val editedList = editableList.map("edited list") { it.edited.now }

    override val edited: ReactiveValue<List<E?>> =
            binding<List<E?>>("edited", dependencies(editedList)) { editedList.now as List<E?> }
    override val isOk: ReactiveBoolean = TODO()
}