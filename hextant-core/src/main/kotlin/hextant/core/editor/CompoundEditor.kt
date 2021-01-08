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
import reaktive.value.ReactiveValue
import reaktive.value.now
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.memberProperties

/**
 * Base class for editors that are composed of multiple sub-editors.
 */
abstract class CompoundEditor<R>(context: Context, strategy: ResultStrategy<R>? = null) :
    AbstractEditor<R, EditorView>(context, strategy) {
    /**
     * Composes a result from the component editor results of this compound editor using the [compose] block.
     * The result is updated every time one of the [children] of this [CompoundEditor] changes its result
     * and if any of the component results is incomplete the compound result will be set to the default of the [resultStrategy].
     */
    inline fun compose(crossinline compose: ResultComposer.() -> R): ReactiveValue<R> =
        composeResult(children.now, resultStrategy::default, compose)

    /**
     * Default result used when one of the component editors results is `null` or if composition fails.
     */
    protected open fun defaultResult(): R? = null

    override fun getSubEditor(accessor: EditorAccessor): Editor<*> {
        if (accessor !is PropertyAccessor) throw InvalidAccessorException(accessor)
        val prop = this::class.memberProperties.find { it.name == accessor.propertyName }
            ?: throw InvalidAccessorException(accessor)
        val res = prop.call(this)
        if (res !is Editor<*>) throw InvalidAccessorException(accessor)
        return res
    }

    /**
     * Make the given [editor] a child of this [CompoundEditor].
     */
    protected fun <E : Editor<*>> child(editor: E): PropertyDelegateProvider<CompoundEditor<*>, ReadOnlyProperty<Any?, E>> =
        PropertyDelegateProvider { thisRef, property ->
            @Suppress("DEPRECATION") editor.setAccessor(PropertyAccessor(property.name))
            @Suppress("DEPRECATION") editor.initParent(thisRef)
            thisRef.addChild(editor)
            ReadOnlyProperty { _, _ -> editor }
        }

    override fun supportsCopyPaste(): Boolean = children.now.all { e -> e.supportsCopyPaste() }

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
}