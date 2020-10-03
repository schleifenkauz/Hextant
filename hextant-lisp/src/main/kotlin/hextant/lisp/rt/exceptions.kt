/**
 * @author Nikolaus Knop
 */

package hextant.lisp.rt

import hextant.lisp.SExpr
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class LispRuntimeException(message: String) : Exception(message)

@OptIn(ExperimentalContracts::class)
inline fun ensure(condition: Boolean, message: () -> String) {
    contract {
        returns() implies condition
    }
    if (!condition) fail(message())
}

inline fun ensure(condition: SExpr, message: () -> String) {
    ensure(truthy(condition), message)
}

fun fail(message: String): Nothing {
    throw LispRuntimeException(message)
}