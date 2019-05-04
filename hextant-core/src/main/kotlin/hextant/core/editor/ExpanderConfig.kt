/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor
import hextant.completion.*
import java.util.*

class ExpanderConfig<E : Editor<*>> {
    private val constant = mutableMapOf<String, (Context) -> E>()
    private val interceptors = LinkedList<(String, Context) -> E?>()

    fun completer(strategy: CompletionStrategy, factory: CompletionFactory<String>): Completer<String> =
        ConfiguredCompleter(strategy, factory, pool = { constant.keys })

    fun registerConstant(text: String, create: (Context) -> E) {
        constant[text] = create
    }

    fun registerInterceptor(interceptor: (text: String, Context) -> E?) {
        interceptors.add(interceptor)
    }

    fun expand(text: String, context: Context): E? {
        val constant = constant[text]
        if (constant != null) return constant(context)
        for (i in interceptors) {
            val e = i(text, context)
            if (e != null) return e
        }
        return null
    }
}