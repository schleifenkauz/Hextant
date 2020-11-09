/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.Editor
import reaktive.list.MutableReactiveList
import reaktive.value.ReactiveValue
import reaktive.value.binding.binding

/**
 * @see AbstractListEditor
 */
abstract class ListEditor<R : Any, E : Editor<R>>(context: Context, editors: MutableReactiveList<E>) :
    AbstractListEditor<R, E, List<R>>(context, editors) {
    override val result: ReactiveValue<List<R>> = binding<List<R>>(results) { results.now }
}