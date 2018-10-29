/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.editor

import org.nikok.hextant.Editable
import java.util.*

class ExpanderConfig<E : Editable<*>, Ex: Expander<E>> {
    private val constant = mutableMapOf<String, Ex.() -> E>()
    private val interceptors = LinkedList<Ex.(String) -> E?>()

    fun registerConstant(text: String, create: Ex.() -> E) {
        constant[text] = create
    }

    fun registerInterceptor(interceptor: Ex.(String) -> E?) {
        interceptors.add(interceptor)
    }

    fun expand(text: String, expander: Ex): E? {
        val constant = constant[text]
        if (constant != null) return constant(expander)
        for (i in interceptors) {
            val e = i(expander, text)
            if (e != null) return e
        }
        return null
    }

    fun getCompletions(): Set<String> = constant.keys
}