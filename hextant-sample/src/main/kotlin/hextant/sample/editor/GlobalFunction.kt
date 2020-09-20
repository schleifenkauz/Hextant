package hextant.sample.editor

import hextant.sample.*

data class GlobalFunction(val returnType: SimpleType?, val name: Identifier, val parameters: List<Parameter?>) {
    override fun toString(): String = buildString {
        append(returnType ?: "<error>")
        append(' ')
        append(name)
        parameters.joinTo(this, separator = ", ", prefix = "(", postfix = ")") { p -> p?.toString() ?: "?" }
    }
}