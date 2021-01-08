package hextant.core.editor

import validated.*

interface ResultStrategy<R> {
    fun default(): R

    fun unwrap(result: R): Any?

    fun isValid(result: R): Boolean

    fun chooseFrom(vararg results: R): R = results.firstOrNull { isValid(it) } ?: results.firstOrNull() ?: default()

    class Validate<R : Any> : ResultStrategy<Validated<R>> {
        override fun default(): Validated<R> = invalidComponent()

        override fun unwrap(result: Validated<R>): R? = result.orNull()

        override fun isValid(result: Validated<R>): Boolean = result.isValid
    }

    class Nullable<R : Any> : ResultStrategy<R?> {
        override fun default(): R? = null

        override fun unwrap(result: R?): R? = result

        override fun isValid(result: R?): Boolean = result != null
    }

    class Simple<R : Any> : ResultStrategy<R> {
        override fun default(): R =
            throw UnsupportedOperationException("default value is not implemented for SimpleResultStrategy")

        override fun unwrap(result: R): R = result

        override fun isValid(result: R): Boolean = true
    }

}