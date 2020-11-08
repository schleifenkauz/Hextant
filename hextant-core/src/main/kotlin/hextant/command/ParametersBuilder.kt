package hextant.command

import hextant.command.Command.Parameter
import hextant.command.Command.ParameterBuilder

/**
 * Builder for [Parameter]s
 */
@Builder
class ParametersBuilder @PublishedApi internal constructor() {
    @PublishedApi internal val parameters: MutableList<Parameter<*>> = mutableListOf()

    /**
     * Add a parameter build with [block]
     */
    inline fun <reified P : Any> add(block: ParameterBuilder<P>.() -> Unit) {
        val p = parameter(block)
        parameters.add(p)
    }

    /**
     * Add a parameter with the specified [name] applying [block] to it
     */
    inline fun <reified P : Any> add(name: String, block: ParameterBuilder<P>.() -> Unit) {
        add<P> {
            this.name = name
            block()
        }
    }

    /**
     * Add a parameter named with this [String] applying [block] to it
     */
    inline operator fun <reified P : Any> String.invoke(block: ParameterBuilder<P>.() -> Unit) {
        add(this, block)
    }

    @PublishedApi internal fun build(): List<Parameter<*>> = parameters
}
