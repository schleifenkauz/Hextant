/**
 *@author Nikolaus Knop
 */

@file:Suppress("LeakingThis")

package org.nikok.hextant.core.editable

import kserial.*
import org.nikok.hextant.Editable
import org.nikok.reaktive.Observer
import org.nikok.reaktive.value.*

abstract class Expandable<N, E : Editable<N>> : Editable<N?>, Serializable {
    final override val isOk: ReactiveBoolean = reactiveValue("isOk", true)
    private val _edited: ReactiveVariable<N?> = reactiveVariable("Edited of $this", null)
    private val _editable: ReactiveVariable<E?> = reactiveVariable("content of $this", null)
    private var bindObserver: Observer? = null

    private val _text: ReactiveVariable<String> = reactiveVariable("Text of $this", "")

    private val _isExpanded: ReactiveVariable<Boolean> = reactiveVariable("$this is expanded", false)

    val isExpanded: ReactiveValue<Boolean> get() = _isExpanded

    fun setText(text: String) {
        bindObserver?.kill()
        bindObserver = null
        _isExpanded.set(false)
        _edited.set(null)
        _editable.set(null)
        _text.set(text)
    }

    fun setContent(editable: E) {
        _text.set("")
        _editable.set(editable)
        _isExpanded.set(true)
        bindObserver?.kill()
        bindObserver = _edited.bind(editable.edited)
    }

    val text: ReactiveVariable<String> get() = _text

    val editable: ReactiveValue<E?> get() = _editable

    final override val edited: ReactiveValue<N?>
        get() = _edited

    override val children: Collection<Editable<*>>?
        get() = editable.now?.let { listOf(it) }

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
        if (isExpanded.now) {
            output.writeBoolean(true)
            output.writeObject(editable.now!!, context)
        } else {
            output.writeBoolean(false)
            output.writeString(text.now)
        }
    }
}