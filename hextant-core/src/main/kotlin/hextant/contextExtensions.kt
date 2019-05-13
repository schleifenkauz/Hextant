/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import kotlin.reflect.KClass

fun Context.createView(editor: Editor<*>, arguments: Bundle = Bundle.newInstance()): EditorControl<*> =
    try {
        get(Public, EditorControlFactory).getControl(editor, arguments)
    } catch (e: NoSuchElementException) {
        parent?.createView(editor) ?: throw e
    }

fun <R : Any> Context.createEditor(resultCls: KClass<R>): Editor<R> =
    try {
        get(Public, EditorFactory).getEditor(resultCls, this)
    } catch (e: NoSuchElementException) {
        parent?.createEditor(resultCls) ?: throw e
    }

fun <R : Any> Context.createEditor(result: R): Editor<R> =
    try {
        get(Public, EditorFactory).getEditor(result, this)
    } catch (e: NoSuchElementException) {
        parent?.createEditor(result) ?: throw e
    }

@JvmName("getPublic")
operator fun <T : Any> Context.get(property: Property<T, Public, *>): T = get(Public, property)

@JvmName("setPublic")
operator fun <T : Any> Context.set(property: Property<T, *, Public>, value: T) =
    set(Public, property, value)

@JvmName("getInternal")
internal operator fun <T : Any> Context.get(property: Property<T, Internal, *>): T = get(
    Internal, property
)

@JvmName("setInternal")
internal operator fun <T : Any> Context.set(property: Property<T, *, Internal>, value: T) =
    set(Internal, property, value)

fun Context.runLater(action: () -> Unit) = platform.runLater(action)