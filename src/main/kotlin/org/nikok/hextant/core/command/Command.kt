/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command

import kotlin.reflect.KClass

/**
 * A Command that is executable on a receiver of type [R]
 */
interface Command<in R : Any, out T> {
    /**
     * Execute this command on [receiver] with the specified [args]
     */
    fun execute(receiver: R, vararg args: Any?): T

    /**
     * @return the short name of this [Command]
     * * This should be a short "typeable" name because it is used in the command shell
     * * For example: refactor
     * * If [shortName] returns `null` this [Command] cannot be used from the command line
     */
    val shortName: String?

    /**
     * @return the name of this [Command]
     * * It should be a imperative description of the action this command executes
     * * For example: 'Refactor this Method'
    */
    val name: String

    /**
     * @return he category of this [Command]
     * * This is used to place the menu item in the right menu of the menu bar
     * * If [category] returns `null` this [Command] can't be used from the Menu bar
     */
    val category: Category?

    /**
     * The parameters of this [Command]
    */
    val parameters: List<Parameter>

    /**
     * @return the description of this [Command]
     * * It should explain what this command does
     */
    val description: String

    val receiverCls: KClass<in R>

    /**
     * @return whether this [Command] can be executed on the specified [receiver]
    */
    fun isApplicableOn(receiver: Any): Boolean

    class Category private constructor(val name: String) {
        companion object {
            private val cache = mutableMapOf<String, Category>()

            fun withName(name: String) = cache.getOrPut(name) { Category(name) }

            val FILE = Category.withName("File")
            val EDIT = Category.withName("Edit")
            val VIEW = Category.withName("View")
        }
    }

    /**
     * A Parameter of a [Command]
     * @property name the name of this parameter
     * @property type the expected type for this parameter
     * @property nullable specifies whether the type should be nullable
     * @property description explains what this parameter is used for
     */
    data class Parameter(
        val name: String, val type: KClass<*>, val nullable: Boolean = false, val description: String
    ) {
        override fun toString() = buildString {
            append(name)
            append(": ")
            append(type.simpleName ?: "<ERROR>")
            if (nullable) append("?")
            appendln()
            append(description)
        }
    }

    @Builder
    class ParameterBuilder @PublishedApi internal constructor() {
        lateinit var name: String
        lateinit var type: KClass<*>
        var nullable = false
        var description: String = "No description provided"

        inline fun <reified T> ofType() {
            type = T::class
        }

        @PublishedApi internal fun build(): Parameter = Parameter(name, type, nullable, description)
    }
}