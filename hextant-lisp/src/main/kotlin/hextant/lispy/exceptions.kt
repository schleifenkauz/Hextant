/**
 * @author Nikolaus Knop
 */

package hextant.lispy

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