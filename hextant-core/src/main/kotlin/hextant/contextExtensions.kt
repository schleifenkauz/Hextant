/**
 * @author Nikolaus Knop
 */

package hextant

import hextant.base.EditorControl
import hextant.bundle.Bundle
import hextant.bundle.CorePermissions.Internal
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.core.*
import kotlin.reflect.KClass

fun Context.createView(editable: Editable<*>, arguments: Bundle = Bundle.newInstance()): EditorControl<*> =
    try {
        get(Public, EditorControlFactory).getControl(editable, this, arguments)
    } catch (e: NoSuchElementException) {
        parent?.createView(editable) ?: throw e
    }

fun <E : Editable<*>, Ed : Editor<E>> Context.getEditor(editable: E): Ed =
    try {
        @Suppress("UNCHECKED_CAST")
        get(Public, EditorFactory).getEditor(editable, this) as Ed
    } catch (e: NoSuchElementException) {
        parent?.getEditor(editable) ?: throw e
    }

fun <T : Any> Context.getEditable(edited: T): Editable<T> =
    try {
        get(Public, EditableFactory).getEditable(edited)
    } catch (e: NoSuchElementException) {
        parent?.getEditable(edited) ?: throw e
    }

fun <T : Any> Context.getEditable(cls: KClass<T>): Editable<T> =
    try {
        get(Public, EditableFactory).getEditable(cls)
    } catch (e: NoSuchElementException) {
        parent?.getEditable(cls) ?: throw e
    }

@JvmName("getPublic")
operator fun <T : Any> Context.get(property: Property<T, Public, *>): T = get(
    Public, property
)

@JvmName("setPublic")
internal operator fun <T : Any> Context.set(property: Property<T, *, Public>, value: T) =
    set(Public, property, value)

@JvmName("getInternal")
internal operator fun <T : Any> Context.get(property: Property<T, Internal, *>): T = get(
    Internal, property
)

@JvmName("setInternal")
internal operator fun <T : Any> Context.set(property: Property<T, *, Internal>, value: T) =
    set(Internal, property, value)

fun Context.runLater(action: () -> Unit) = platform.runLater(action)