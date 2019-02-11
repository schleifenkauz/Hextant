/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Editable
import hextant.completion.*
import reaktive.set.unmodifiableReactiveSet
import java.util.*

class ExpanderConfig<E : Editable<*>> {
    private val constant = mutableMapOf<String, () -> E>()
    private val interceptors = LinkedList<(String) -> E?>()

    fun completer(strategy: CompletionStrategy, factory: CompletionFactory<String>): Completer<String> =
        ConfiguredCompleter(strategy, factory, pool = unmodifiableReactiveSet(constant.keys))

    fun registerConstant(text: String, create: () -> E) {
        constant[text] = create
    }

    fun registerInterceptor(interceptor: (text: String) -> E?) {
        interceptors.add(interceptor)
    }

    fun expand(text: String): E? {
        val constant = constant[text]
        if (constant != null) return constant()
        for (i in interceptors) {
            val e = i(text)
            if (e != null) return e
        }
        return null
    }
}