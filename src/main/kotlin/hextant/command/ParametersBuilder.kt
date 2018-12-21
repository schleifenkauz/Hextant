package hextant.command

import hextant.command.Command.Parameter
import hextant.command.Command.ParameterBuilder

/**
 * Builder for [Parameter]s
*/
@Builder
class ParametersBuilder @PublishedApi internal constructor() {
    @PublishedApi internal val parameters: MutableList<Command.Parameter> = mutableListOf()

    /**
     * Add a parameter build with [block]
    */
    inline fun add(block: ParameterBuilder.() -> Unit) {
        val p = parameter(block)
        parameters.add(p)
    }

    /**
     * Add a parameter with the specified [name] applying [block] to it
    */
    inline fun add(name: String, block: ParameterBuilder.() -> Unit) {
        add {
            this.name = name
            block()
        }
    }

    /**
     * Add a parameter named with this [String] applying [block] to it
     */
    inline operator fun String.invoke(block: ParameterBuilder.() -> Unit) {
        add(this, block)
    }

    @PublishedApi internal fun build(): List<Parameter> = parameters
}
