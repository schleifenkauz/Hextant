/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant

import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorFactory
import org.nikok.hextant.core.EditorViewFactory
import kotlin.reflect.KClass

fun Context.createView(editable: Editable<*>) = get(Public, EditorViewFactory).getFXView(editable)

fun <E : Editable<*>> Context.resolveEditor(editable: E) = get(Public, EditorFactory).resolveEditor(editable)

fun <E : Editable<*>, Ed : Editor<E>> Context.getEditor(editable: E, cls: KClass<Ed>) =
    get(Public, EditorFactory).getEditor(cls, editable)

inline fun <E : Editable<*>, reified Ed : Editor<E>> Context.getEditor(editable: E) = getEditor(editable, Ed::class)

