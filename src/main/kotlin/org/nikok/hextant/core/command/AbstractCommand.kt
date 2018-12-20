/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command

import org.nikok.hextant.core.command.Command.Category
import kotlin.reflect.KClass

/**
 * Skeletal implementation of [Command]
 * @constructor
 */
abstract class AbstractCommand<R : Any, T>(override val receiverCls: KClass<R>) : Command<R, T> {
    override val shortName: String?
        get() = null
    override val category: Category?
        get() = null

    private fun checkArgs(args: Array<out Any?>) {
        if (args.size > parameters.size) throw ArgumentMismatchException("To many arguments for command $this")
        if (args.size < parameters.size) throw ArgumentMismatchException("To few arguments for command $this")
        for ((a, p) in args.zip(parameters)) {
            if (a == null && !p.nullable) throw ArgumentMismatchException("Null argument for non-nullable parameter $p")
            if (!p.type.isInstance(a)) throw ArgumentMismatchException("Parameter $a is not an instance of ${p.type}")
        }
    }

    /**
     * Execute this [Command] on the specified [receiver] and the specified [args]
     */
    protected abstract fun doExecute(receiver: R, args: Array<out Any?>): T

    /**
     * Check that the specified [args] match [parameters] and then call [doExecute]
     */
    final override fun execute(receiver: R, vararg args: Any?): T {
        checkArgs(args)
        return doExecute(receiver, args)
    }

    /**
     * By default return `true` if the specified [receiver] is an instance of [R]
     */
    override fun isApplicableOn(receiver: Any) = receiverCls.isInstance(receiver)

    final override fun toString(): String = buildString {
        append(shortName)
        appendln(": ")
        for (p in parameters) {
            appendln(p)
        }
        appendln(description)
    }
}