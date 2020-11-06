/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.EditorView
import hextant.serial.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import reaktive.value.now
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
            editor.setAccessor(PropertyAccessor(property.name))
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

    override fun createSnapshot(): Snapshot<*> = Snap()

    private class Snap : Snapshot<CompoundEditor<*>>() {
        private lateinit var snapshots: List<Pair<String, Snapshot<Editor<*>>>>

        private fun getComponentName(comp: Editor<*>): String {
            val acc = comp.accessor.now ?: error("Editor $comp has no accessor")
            check(acc is PropertyAccessor) { "Children of compound editors must have property accessors, but $comp is accessed by $acc" }
            return acc.propertyName
        }

        override fun doRecord(original: CompoundEditor<*>) {
            snapshots = original.children.now.map { e -> getComponentName(e) to e.snapshot() }
        }

        override fun reconstruct(original: CompoundEditor<*>) {
            for ((comp, p) in original.children.now.zip(snapshots)) {
                val (_, snap) = p
                snap.reconstruct(comp)
            }
        }

        override fun JsonObjectBuilder.encode() {
            for ((name, snap) in snapshots) {
                put(name, snap.encode())
            }
        }

        override fun decode(element: JsonObject) {
            snapshots = element.entries
                .filter { (prop) -> !prop.startsWith('_') }
                .map { (prop, value) -> prop to decode<Editor<*>>(value) }
        }
    }

    override fun supportsCopyPaste(): Boolean = children.now.all { e -> e.supportsCopyPaste() }

    companion object {
        private fun <T, R> delegate(value: T): ReadOnlyProperty<R, T> = ReadOnlyProperty { _, _ -> value }
    }
}