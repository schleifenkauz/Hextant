/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.bundle.Property
import org.nikok.hextant.core.CorePermissions.Internal
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.EditorViewFactory
import kotlin.reflect.KClass

fun Context.createView(editable: Editable<*>) = get(Public, EditorViewFactory).getFXView(editable)

fun <E : Editable<*>> Context.resolveEditor(editable: E) = get(Public, EditorFactory).resolveEditor(editable)

fun <E : Editable<*>, Ed : Editor<E>> Context.getEditor(editable: E, cls: KClass<Ed>) =
    get(Public, EditorFactory).getEditor(cls, editable)

inline fun <E : Editable<*>, reified Ed : Editor<E>> Context.getEditor(editable: E) = getEditor(editable, Ed::class)

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