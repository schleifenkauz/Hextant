/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.rt

class SampleRuntimeException(msg: String?, cause: Throwable?) : RuntimeException(msg, cause) {
    constructor(cause: Throwable) : this(cause.message, cause)

    constructor(msg: String) : this(msg, null)

    constructor() : this(null, null)
}