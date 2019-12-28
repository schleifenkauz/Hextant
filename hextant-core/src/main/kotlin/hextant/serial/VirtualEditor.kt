/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor
import kotlin.reflect.KProperty

/**
 * Virtual editors consist of a [HextantFile] with a [RootEditor] and an [EditorLocation].
 * They weakly cache the editor denoted by the [EditorLocation] and localize it from the root if needed.
 */
interface VirtualEditor<E : Editor<*>> {
    /**
     * If the wrapped editors is still in memory, just return it. Otherwise get the root and resolve it from there.
     */
    fun get(): E

    /**
     * Alias for [get]
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): E = get()
}