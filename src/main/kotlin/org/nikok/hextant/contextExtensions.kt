/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorControlFactory
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.base.EditorControl

fun Context.createView(editable: Editable<*>): EditorControl<*> =
    try {
        get(Public, EditorControlFactory).getControl(editable, this)
    } catch (e: NoSuchElementException) {
        parent?.createView(editable) ?: throw e
    }

fun <E : Editable<*>> Context.getEditor(editable: E): Editor<E> =
    try {
        get(Public, EditorFactory).getEditor(editable, this)
    } catch (e: NoSuchElementException) {
        parent?.getEditor(editable) ?: throw e
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