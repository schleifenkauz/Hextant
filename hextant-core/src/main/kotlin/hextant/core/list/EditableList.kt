/**
 *@author Nikolaus Knop
 */

package hextant.core.list

import hextant.Editable
import kserial.*
import reaktive.collection.binding.allR
import reaktive.dependencies
import reaktive.list.binding.values
import reaktive.list.reactiveList
import reaktive.value.ReactiveBoolean
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding

open class EditableList<N, E : Editable<N>> :
    Editable<List<N>>, Serializable {
    val editableList = reactiveList<E>()

    val editedList = editableList.map { it.edited }.values()

    @Suppress("UNCHECKED_CAST")
    override val edited: ReactiveValue<List<N>?> =
        binding<List<N>?>(dependencies(editedList)) {
            editedList.now.takeIf { it.none { el -> el == null } } as List<N>?
        }

    override val isOk: ReactiveBoolean get() = editableList.allR { it.isOk }

    override fun deserialize(input: Input, context: SerialContext) {
        editableList.now.addAll(input.readTyped(context)!!)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeObject(editableList.now.toList(), context)
    }
}