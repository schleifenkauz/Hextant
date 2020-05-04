/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.*
import hextant.serial.*
import kserial.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for editors that are composed of multiple sub-editors.
 */
abstract class CompoundEditor<R : Any>(context: Context) :
    AbstractEditor<R, EditorView>(context), Serializable {
    private val constructor = this::class.primaryConstructor!!

    /**
     * Make the given [editor] a child of this [CompoundEditor].
     */
    protected fun <E : Editor<*>> child(editor: E): ChildDelegator<E> = ChildDelegatorImpl(editor)

    override fun serialize(output: Output, context: SerialContext) {
        for (c in children.now) {
            output.writeUntyped(c)
        }
    }

    override fun deserialize(input: Input, context: SerialContext) {
        for (c in children.now) {
            input.readInplace(c)
        }
    }

    override fun paste(editor: Editor<*>): Boolean {
        if (editor.javaClass != this.javaClass) return false
        for ((original, copy) in editor.children.now.zip(this.children.now)) {
            val supported = copy.paste(original)
            check(supported) { "Unsupported copy" }
        }
        return true
    }

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
            editor.initAccessor(PropertyAccessor(property.name))
            editor.initParent(this@CompoundEditor)
            addChild(editor)
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