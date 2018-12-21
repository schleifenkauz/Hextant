/**
 *@author Nikolaus Knop
 */

package hextant.base

import hextant.Context

abstract class DefaultRegistration(private val doRegister: Context.() -> Unit) {
    private val registeredPlatforms = mutableSetOf<Context>()

    fun registerDefault(context: Context) {
        if (!registeredPlatforms.add(context)) return
        context.doRegister()
    }
}