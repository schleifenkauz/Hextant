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
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

/**
 * Base class for editors that are composed of multiple sub-editors.
 */
abstract class CompoundEditor<R>(context: Context) : AbstractEditor<R, EditorView>(context) {
    private val resultType = this::class.memberFunctions.first { it.name == "defaultResult" }.returnType

    /**
     * Composes a result from the component editor results of this compound editor using the [compose] block.
     * The result is updated every time one of the [children] of this [CompoundEditor] changes its result
     * and if any of the component results is incomplete the compound result will be set to the specified [default].
     */
    inline fun composeResult(
        crossinline default: () -> R = ::defaultResult,
        crossinline compose: ResultComposer.() -> R
    ): ReactiveValue<R> = composeResult(children.now, default, compose)

    /**
     * Returns the result that this token editor should have if it one of its components has an invalid result.
     *
     * You must override this method if the result type of your editor is not nullable.
     * Otherwise the default implementation will throw an [IllegalStateException].
     * If the default implementation is called on a token editor whose result type is nullable it just returns null.
     */
    @Suppress("UNCHECKED_CAST")
    open fun defaultResult(): R =
        if (resultType.isMarkedNullable) null as R
        else error("CompoundEditor ${this::class}: non-nullable result type and defaultResult() was not overwritten")

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

        override fun reconstructObject(original: CompoundEditor<*>) {
            for ((comp, p) in original.children.now.zip(snapshots)) {
                val (_, snap) = p
                snap.reconstructObject(comp)
            }
        }

        override fun encode(builder: JsonObjectBuilder) {
            for ((name, snap) in snapshots) {
                builder.put(name, snap.encodeToJson())
            }
        }

        override fun decode(element: JsonObject) {
            snapshots = element.entries
                .filter { (prop) -> !prop.startsWith('_') }
                .map { (prop, value) -> prop to decodeFromJson<Editor<*>>(value) }
        }
    }
}