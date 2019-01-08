package hextant.lisp

import hextant.lisp.SinglyLinkedList.Empty

sealed class Value {
    abstract override fun toString(): String

    abstract val jvm: Any

    open fun toBoolean(): Boolean = true

    abstract fun apply(arguments: List<SExpr>): Value

    companion object {
        fun of(value: Any?): Value = when (value) {
            is Int                 -> IntegerValue(value)
            is Double              -> DoubleValue(value)
            is Char                -> CharValue(value)
            is String              -> StringValue(value)
            is SinglyLinkedList<*> -> ListValue(value.map { Value.of(it) })
            is List<*>             -> ListValue(SinglyLinkedList.fromList(value.map { Value.of(it) }))
            else                   -> throw IllegalArgumentException("No LISP representation of $value")
        }
    }
}

data class Lambda(val parameters: List<String>, val body: SExpr) : Value() {
    override fun toString(): String = Util.lambdaToString(parameters, body)

    override fun apply(arguments: List<SExpr>): Value = parameters
        .zip(arguments.map(::LazyExpr))
        .fold(body) { acc, (param, arg) ->
            acc.replaceOccurrencesOf(param, arg)
        }
        .evaluate()

    override val jvm: (List<SExpr>) -> Value
        get() = this::apply
}

sealed class ScalarValue : Value() {
    override fun apply(arguments: List<SExpr>): Value =
        throw LispRuntimeError("Scalar value cannot be applied to arguments")
}

data class IntegerValue(val value: Int) : ScalarValue() {
    override fun toString(): String = value.toString()

    override fun toBoolean(): Boolean = value != 0

    override val jvm: Int
        get() = value
}

data class DoubleValue(val value: Double) : ScalarValue() {
    override fun toString(): String = value.toString()

    override val jvm: Double
        get() = value

    override fun toBoolean(): Boolean = value != 0.0
}

data class CharValue(val value: Char) : ScalarValue() {
    override val jvm: Char
        get() = value

    override fun toBoolean(): Boolean = value.toInt() != 0

    override fun toString(): String = value.toString()
}

data class StringValue(val value: String) : ScalarValue() {
    override fun toString(): String = value

    override fun toBoolean(): Boolean = value != ""

    override val jvm: String
        get() = value
}

data class ListValue(val elements: SinglyLinkedList<Value>) : Value() {
    override fun toString(): String = elements.joinToString(prefix = "(", separator = " ", postfix = ")")

    override fun toBoolean(): Boolean = elements != Empty

    override val jvm: SinglyLinkedList<Value>
        get() = elements

    override fun apply(arguments: List<SExpr>): Value =
        throw LispRuntimeError("List value cannot be applied to arguments")
}

data class BuiltInFunction(
    val name: Identifier,
    val arity: Int,
    val function: (List<Value>) -> Value
) : Value() {
    override val jvm: (List<Value>) -> Value
        get() = function

    override fun apply(arguments: List<SExpr>): Value = when {
        arguments.size > arity ->
            throw LispRuntimeError("Too many arguments for $name, expected $arity, but got ${arguments.size}")
        arguments.size < arity -> BuiltInFunction(name, arity - arguments.size) { args ->
            function(arguments.map { it.evaluate() } + args)
        }
        else                   -> function(arguments.map { it.evaluate() })
    }
}