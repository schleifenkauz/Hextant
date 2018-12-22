/**
 *@author Nikolaus Knop
 */

package hextant.core.list

import hextant.Editable
import hextant.ParentEditable
import kserial.*
import org.nikok.reaktive.collection.all
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.list.binding.values
import org.nikok.reaktive.list.reactiveList
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.binding.binding
import kotlin.reflect.KClass

open class EditableList<N, E : Editable<N>>(private val elementCls: KClass<out E>) :
    ParentEditable<List<E?>, E>(), Serializable {
    val editableList = reactiveList<E>("editable list")

    val editedList = editableList.map("edited list") { it.edited }.values()

    override val children: Collection<E>
        get() = editableList.now

    override val edited: ReactiveValue<List<E?>> =
        binding<List<E?>>("edited", dependencies(editedList)) { editableList.now }

    override val isOk: ReactiveBoolean get() = editableList.all("all ok") { it.isOk }

    override fun accepts(child: Editable<*>): Boolean = elementCls.isInstance(child)

    override fun deserialize(input: Input, context: SerialContext) {
        editableList.now.addAll(input.readTyped(context)!!)
    }

    override fun serialize(output: Output, context: SerialContext) {
        output.writeObject(editableList.now.toList(), context)
    }
}