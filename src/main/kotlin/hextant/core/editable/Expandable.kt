/**
 *@author Nikolaus Knop
 */

@file:Suppress("LeakingThis")

package hextant.core.editable

import hextant.Editable
import hextant.ParentEditable
import kserial.*
import org.nikok.reaktive.value.*

abstract class Expandable<N, E : Editable<N>> : ParentEditable<N, E>(), Serializable {
    final override val isOk: ReactiveBoolean = reactiveValue("isOk", true)

    private val _editable: ReactiveVariable<E?> = reactiveVariable("content of $this", null)

    private val _text: ReactiveVariable<String> = reactiveVariable("Text of $this", "")

    private val _isExpanded: ReactiveVariable<Boolean> = reactiveVariable("$this is expanded", false)

    val isExpanded: ReactiveValue<Boolean> get() = _isExpanded

    fun setText(text: String) {
        _isExpanded.set(false)
        _editable.set(null)
        _text.set(text)
    }

    fun setContent(editable: E) {
        _text.set("")
        _editable.set(editable)
        _isExpanded.set(true)
        editable.moveTo(this)
    }

    val text: ReactiveVariable<String> get() = _text

    val editable: ReactiveValue<E?> get() = _editable

    final override val edited: ReactiveValue<N?> =
        _editable.flatMap("Edited of $this") { it?.edited ?: reactiveValue("null", null) }

    override val children: Collection<E>
        get() {
            val content = editable.now ?: return emptyList()
            return listOf(content)
        }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(input: Input, context: SerialContext) {
        val expanded = input.readBoolean()
        if (expanded) {
            setContent(input.readObject(context) as E)
        } else {
            setText(input.readString())
        }
    }

    override fun serialize(output: Output, context: SerialContext) {
        val expanded = isExpanded.now
        output.writeBoolean(expanded)
        if (expanded) {
            output.writeObject(editable.now!!, context)
        } else {
            output.writeString(text.now)
        }
    }
}