/**
 *@author Nikolaus Knop
 */

package hextant.command

import kotlin.reflect.KClass

/**
 * A Command that is executable on a receiver of type [R]
 */
interface Command<in R : Any, out T> {
    /**
     * Execute this command on [receiver] with the specified [args]
     */
    fun execute(receiver: R, args: List<Any?>): T

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

    /**
     * The class of the receiver used for checking the type when executing
     */
    val receiverCls: KClass<in R>

    /**
     * @return whether this [Command] can be executed on the specified [receiver]
    */
    fun isApplicableOn(receiver: Any): Boolean

    /**
     * A command category corresponds to a menu of shortcuts.
     * @property name the name of the command category
     */
    class Category private constructor(val name: String) {
        companion object {
            private val cache = mutableMapOf<String, Category>()

            /**
             * @return a possibly cached [Category] with the specified [name]
             */
            fun withName(name: String) = cache.getOrPut(name) { Category(name) }

            /**
             * The file menu
             */
            val FILE = withName("File")
            /**
             * The edit menu
             */
            val EDIT = withName("Edit")
            /**
             * The view menu
             */
            val VIEW = withName("View")
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

    /**
     * A builder for [Parameter]s
     */
    @Builder
    class ParameterBuilder @PublishedApi internal constructor() {
        /**
         * The name of the parameter
         */
        lateinit var name: String
        /**
         * The type of the parameter
         */
        lateinit var type: KClass<*>
        /**
         * `true` iff the parameter is nullable
         */
        var nullable = false
        /**
         * The description of this parameter, an explanation of what the parameter influences.
         * The default description is "No description provided"
         */
        var description: String = "No description provided"

        /**
         * Set the type to the class of [T]
         */
        inline fun <reified T> ofType() {
            type = T::class
        }

        @PublishedApi internal fun build(): Parameter = Parameter(name, type, nullable, description)
    }
}