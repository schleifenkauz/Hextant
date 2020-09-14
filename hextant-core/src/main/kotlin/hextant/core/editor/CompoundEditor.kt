/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.EditorView
import hextant.serial.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Base class for editors that are composed of multiple sub-editors.
 */
abstract class CompoundEditor<R>(context: Context) : AbstractEditor<R, EditorView>(context) {
    private val constructor = this::class.primaryConstructor!!

    /**
     * Make the given [editor] a child of this [CompoundEditor].
     */
    protected fun <E : Editor<*>> child(editor: E): ChildDelegator<E> = ChildDelegatorImpl(editor)

    /**
     * Used as a delegate for children of this [CompoundEditor]
     */
    interface ChildDelegator<E : Editor<*>> {
        /**
         * Provides the delegate
         */
        operator fun provideDelegate(
            thisRef: CompoundEditor<*>,
            property: KProperty<*>
        ): ReadOnlyProperty<CompoundEditor<*>, E>
    }

    private inner class ChildDelegatorImpl<E : Editor<*>>(private val editor: E) :
        ChildDelegator<E> {
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

    override fun createSnapshot(): EditorSnapshot<*> = CompoundEditorSnapshot(this)

    override fun supportsCopyPaste(): Boolean = children.now.all { e -> e.supportsCopyPaste() }

    companion object {
        private fun <T, R> delegate(value: T): ReadOnlyProperty<R, T> = ReadOnlyProperty { _, _ -> value }
    }
}