/**
 *@author Nikolaus Knop
 */

package hextant.core.list

import hextant.*
import kserial.*
import reaktive.dependencies
import reaktive.list.binding.values
import reaktive.list.reactiveList
import reaktive.value.binding.binding

open class EditableList<N, E : Editable<N>> :
    Editable<List<N>>, Serializable {
    val editableList = reactiveList<E>()

    val resultList = editableList.map { it.result }.values()

    @Suppress("UNCHECKED_CAST")
    override val result: RResult<List<N>> =
        binding<CompileResult<List<N>>>(dependencies(resultList)) {
            resultList.now.okIfOrChildErr { resultList.now.all { it.isOk } }.map { els -> els.map { el -> el.force() } }
        }

    override fun deserialize(input: Input, context: SerialContext) {
        editableList.now.addAll(input.readTyped(context)!!)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeObject(editableList.now.toList(), context)
    }
}
