/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.serial.*
import kserial.CompoundSerializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

abstract class CompoundEditor<R : Any>(context: Context) :
    AbstractEditor<R, EditorView>(context), CompoundSerializable {
    private val constructor = this::class.primaryConstructor!!

    protected fun <E : Editor<*>> child(editor: E, ctx: Context = context): ChildDelegator<E> {
        @Suppress("UNCHECKED_CAST")
        val copy = editor.copyForImpl(ctx) as E
        child(copy)
        return ChildDelegatorImpl(copy)
    }

    override fun components(): Sequence<Editor<*>> = children.now.asSequence()

    override fun copyForImpl(context: Context): Editor<R> = constructor.call(context, *children.now.toTypedArray())

    interface ChildDelegator<E : Editor<*>> {
        operator fun provideDelegate(
            thisRef: CompoundEditor<*>,
            property: KProperty<*>
        ): ReadOnlyProperty<CompoundEditor<*>, E>
    }

    private inner class ChildDelegatorImpl<E : Editor<*>>(private val editor: E) : ChildDelegator<E> {
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        override fun provideDelegate(
            thisRef: CompoundEditor<*>,
            property: KProperty<*>
        ): ReadOnlyProperty<CompoundEditor<*>, E> {
            editor.setAccessor(PropertyAccessor(property.name))
            return delegate(editor)
        }
    }

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is PropertyAccessor) throw InvalidAccessorException(accessor)
        val prop = this::class.memberProperties.find { it.name == accessor.propertyName }
            ?: throw InvalidAccessorException(accessor)
        val res = prop.call(this)
        if (res !is Editor<*>) throw InvalidAccessorException(accessor)
        return res
    }

    companion object {
        private fun <T, R> delegate(value: T): ReadOnlyProperty<R, T> = object : ReadOnlyProperty<R, T> {
            override fun getValue(thisRef: R, property: KProperty<*>): T = value
        }
    }
}