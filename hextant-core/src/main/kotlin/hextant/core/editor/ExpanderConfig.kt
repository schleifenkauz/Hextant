/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.Editor
import hextant.completion.*
import java.util.*

/**
 * A configuration for a [ConfiguredExpander]
 */
class ExpanderConfig<E : Editor<*>> {
    private val constant = mutableMapOf<String, (Context) -> E>()
    private val interceptors = LinkedList<(String, Context) -> E?>()

    /**
     * @return a [Completer] which uses the registered choices and the given [strategy] and [factory]
     */
    fun completer(strategy: CompletionStrategy, factory: CompletionFactory<String>): Completer<String> =
        ConfiguredCompleter(strategy, factory, pool = { constant.keys })

    /**
     * If the given string is expanded an editor is returned using [create].
     * Previous invocation with the same [key] are overridden
     */
    fun registerConstant(key: String, create: (Context) -> E) {
        constant[key] = create
    }

    /**
     * On expanding the given [interceptor] is invoked and if it returns a non-null value this value is returned.
     * Interceptors the have been registered **last** are tried **first**
     */
    fun registerInterceptor(interceptor: (text: String, Context) -> E?) {
        interceptors.addFirst(interceptor)
    }

    /**
     * Expand the given [text] in the given [context] using the registered interceptors.
     * If no interceptor matches `null` is returned
     */
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