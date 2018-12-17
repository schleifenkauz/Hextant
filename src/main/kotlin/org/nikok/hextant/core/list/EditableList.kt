/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.list

import org.nikok.hextant.Editable
import org.nikok.hextant.ParentEditable
import org.nikok.reaktive.collection.all
import org.nikok.reaktive.dependencies
import org.nikok.reaktive.list.binding.values
import org.nikok.reaktive.list.reactiveList
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.ReactiveValue
import org.nikok.reaktive.value.binding.binding
import kotlin.reflect.KClass

open class EditableList<N, E : Editable<N>>(private val elementCls: KClass<out E>) :
    ParentEditable<List<E?>, E>() {
    val editableList = reactiveList<E>("editable list")

    val editedList = editableList.map("edited list") { it.edited }.values()

    override val children: Collection<E>
        get() = editableList.now

    override val edited: ReactiveValue<List<E?>> =
        binding<List<E?>>("edited", dependencies(editedList)) { editableList.now }

    override val isOk: ReactiveBoolean get() = editableList.all("all ok") { it.isOk }

    override fun accepts(child: Editable<*>): Boolean = elementCls.isInstance(child)

    companion object {
        inline fun <N, reified E : Editable<N>> newInstance() = EditableList(E::class)
    }
}