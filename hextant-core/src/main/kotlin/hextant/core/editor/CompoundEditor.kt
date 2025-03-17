/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import hextant.core.EditorView
import hextant.serial.EditorAccessor
import hextant.serial.InvalidAccessorException
import hextant.serial.PropertyAccessor
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import reaktive.value.ReactiveValue
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

/**
 * Base class for editors that are composed of multiple sub-editors.
 */
abstract class CompoundEditor<R> : AbstractEditor<R, EditorView>() {
    private val resultType = this::class.memberFunctions.first { it.name == "defaultResult" }.returnType

    /**
     * Composes a result from the component editor results of this compound editor using the [compose] block.
     * The result is updated every time one of the [children] of this [CompoundEditor] changes its result
     * and if any of the component results is incomplete the compound result will be set to the specified [default].
     */
    inline fun composeResult(
        crossinline default: () -> R = ::defaultResult,
        crossinline compose: ResultComposer.() -> R
    ): ReactiveValue<R> = composeResult(getChildren(), default, compose)

    override fun setupDefaultState() {
        for (editor in getChildren()) {
            editor.setupDefaultState()
        }
    }

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
    protected fun <E : Editor<*>> child(
        editor: E, context: Context = this.context
    ): PropertyDelegateProvider<CompoundEditor<*>, ReadOnlyProperty<Any?, E>> =
        PropertyDelegateProvider { _, property ->
            editor.initialize(context)
            addChild(property.name, editor)
            ReadOnlyProperty { _, _ -> editor }
        }

    protected fun addChild(name: String, editor: Editor<*>) {
        val acc = PropertyAccessor(name)
        editor.locate(this, acc)
        addChild(editor)
    }

    override fun supportsCopyPaste(): Boolean = getChildren().all { e -> e.supportsCopyPaste() }

    override fun paste(editor: Editor<*>): Boolean {
        if (this::class.java.isInstance(editor)) {
            editor as CompoundEditor
            val childrenByProperty = mutableMapOf<String, Editor<*>>()
            for (child in getChildren()) {
                val acc = child.accessor as? PropertyAccessor ?: continue
                childrenByProperty[acc.propertyName] = child
            }
            for (child in editor.getChildren()) {
                val acc = child.accessor as? PropertyAccessor ?: continue
                val myChild = childrenByProperty[acc.propertyName] ?: continue
                myChild.paste(child)
            }
        }
        return false
    }

    override fun serialize(): JsonElement = buildJsonObject {
        for (child in getChildren()) {
            val property = child.accessor as? PropertyAccessor ?: continue
            put(property.propertyName, child.serialize())
        }
    }

    override fun deserialize(element: JsonElement) {
        for (child in getChildren()) {
            val property = child.accessor as? PropertyAccessor ?: continue
            val childElement = element.jsonObject.getValue(property.propertyName)
            child.deserialize(childElement)
        }
    }
}