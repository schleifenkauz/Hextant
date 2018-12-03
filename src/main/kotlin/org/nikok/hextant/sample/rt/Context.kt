package org.nikok.hextant.sample.rt

import org.nikok.hextant.sample.ast.*
import org.nikok.hextant.sample.ast.Type.BOOLEAN
import org.nikok.hextant.sample.ast.Type.INT

/**
 * The runtime context of the sample language
 */
interface Context {
    /**
     * Print the specified [value]
     */
    fun print(value: Any?)

    /**
     * Set the int var with the specified [name] to the specified [value]
     */
    fun setInt(name: Name, value: Int)

    /**
     * Set the boolean var with the specified [name] to the specified [value]
     */
    fun setBoolean(name: Name, value: Boolean)

    /**
     * Set the var with the specified [name] of the specified [type] to the specified [value]
     */
    fun setVar(name: Name, type: Type, value: Any?)

    /**
     * Set the var with the specified [name] to the result of evaluating [expr] in this context
     */
    fun setVar(name: Name, expr: Expr<*>)

    /**
     * @return the value of the integer variable with the specified [name]
     * @throws SampleRuntimeException when there is no value assigned to [name]
     */
    fun getInt(name: Name): Int

    /**
     * @return the value of the boolean variable with the specified [name]
     * @throws SampleRuntimeException when there is no value assigned to [name]
     */
    fun getBoolean(name: Name): Boolean

    /**
     * Throw a [SampleRuntimeException] with the specified [message] if [check] is `false`
     */
    fun runtimeCheck(check: Boolean, message: () -> String)

    private class Runtime : Context {
        private val ints = mutableMapOf<String, Int>()

        private val booleans = mutableMapOf<String, Boolean>()

        override fun print(value: Any?) {
            println(value)
        }

        override fun setInt(name: Name, value: Int) {
            ints[name.str] = value
        }

        override fun setBoolean(name: Name, value: Boolean) {
            booleans[name.str] = value
        }

        override fun setVar(name: Name, type: Type, value: Any?) {
            runtimeCheck(type.isInstance(value)) { "$value is not an instance of $type" }
            when (type) {
                INT     -> setInt(name, value as Int)
                BOOLEAN -> setBoolean(name, value as Boolean)
            }
        }

        override fun setVar(name: Name, expr: Expr<*>) {
            setVar(name, expr.type, expr.eval(this))
        }

        override fun getInt(name: Name): Int =
            ints[name.str] ?: throw SampleRuntimeException("No such int $name")

        override fun getBoolean(name: Name): Boolean =
            booleans[name.str] ?: throw SampleRuntimeException("No such boolean $name")

        override fun runtimeCheck(check: Boolean, message: () -> String) {
            if (check) {
                throw SampleRuntimeException(message())
            }
        }


    }

    companion object {
        /**
         * @return a new context
         */
        fun runtime(): Context = Runtime()
    }
}