/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.rt

/**
 * An exception that occurs at runtime of the sample language
 * @constructor
 * @param msg the exception message
 * @param cause the exception cause
 */
class SampleRuntimeException(msg: String?, cause: Throwable?) : RuntimeException(msg, cause) {
    /**
     * @param cause the cause, the message is taken from the [cause]
     */
    constructor(cause: Throwable) : this(cause.message, cause)

    /**
     * A [SampleRuntimeException] without a cause
     * @param msg the message
     */
    constructor(msg: String) : this(msg, null)

    /**
     * A [SampleRuntimeException] without a message or cause
     *
     */
    constructor() : this(null, null)
}