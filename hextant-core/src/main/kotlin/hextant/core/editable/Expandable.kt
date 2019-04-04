/**
 *@author Nikolaus Knop
 */

@file:Suppress("LeakingThis")

package hextant.core.editable

import hextant.*
import hextant.base.AbstractEditable
import kserial.*
import reaktive.value.*
import reaktive.value.binding.flatMap
import reaktive.value.binding.map

/**
 * An [Editable] that either holds text or another editable, it can have two states
 * * Not expanded: Holds text
 * * Expanded: Holds the expanded content
 */
abstract class Expandable<N, E : Editable<N>> : AbstractEditable<N>(), Serializable {
    private val _editable: ReactiveVariable<E?> = reactiveVariable(null)

    private val _text: ReactiveVariable<String> = reactiveVariable("")

    private val _isExpanded: ReactiveVariable<Boolean> = reactiveVariable(false)

    /**
     * The edited object of the current [editable], or `null` if this Expandable is not expanded
     */
    final override val result: RResult<N> =
        _editable.flatMap { it?.result?.map { res -> res.or(ChildErr) } ?: reactiveValue(ChildErr) }

    /**
     * Sets the text to [text]
     * * State is now "not expanded" and the [editable] is null
     */
    fun setText(text: String) {
        _isExpanded.set(false)
        _editable.set(null)
        _text.set(text)
    }

    /**
     * Sets the content to the specified [editable]
     * State is now "expanded" and the [text] is an empty string
     */
    fun setContent(editable: E) {
        _text.set("")
        _editable.set(editable)
        _isExpanded.set(true)
    }

    /**
     * Whether this [Expandable] is expanded or not
     */
    val isExpanded: ReactiveValue<Boolean> get() = _isExpanded

    /**
     * The text held by this [Expandable]
     */
    val text: ReactiveVariable<String> get() = _text

    /**
     * The content of this [Expandable]
     */
    val editable: ReactiveValue<E?> get() = _editable

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