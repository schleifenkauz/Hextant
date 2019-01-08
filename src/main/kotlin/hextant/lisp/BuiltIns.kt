package hextant.lisp

object BuiltIns {
    private val let = BuiltInFunction("let", arity = 3) { (name, value, body) ->
        Lambda(listOf(name.toString()), ConstantExpr(body)).apply(listOf(ConstantExpr(value)))
    }

    private val plus = BuiltInFunction("+", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 + operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 + operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 + operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 + operand2)
            operand1 is String && operand2 is String -> StringValue(operand1 + operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val minus = BuiltInFunction("-", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 - operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 - operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 - operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 - operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val times = BuiltInFunction("*", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 * operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 * operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 * operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 * operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val div = BuiltInFunction("/", arity = 2) { (op1, op2) ->
        val operand1 = op1.jvm
        val operand2 = op2.jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 / operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 / operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 / operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 / operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val head = BuiltInFunction("head", arity = 1) { (lst) ->
        if (lst !is ListValue) throw LispRuntimeError("'head' expected a list but got $lst")
        lst.elements.head
    }

    private val tail = BuiltInFunction("tail", arity = 1) { (lst) ->
        if (lst !is ListValue) throw LispRuntimeError("'tail' expected a list but got $lst")
        ListValue(lst.elements.tail)
    }

    private val string = BuiltInFunction("string", arity = 1) { (chars) ->
        if (chars !is ListValue) throw LispRuntimeError("'string' expected a list of chars but got $chars")
        val characters = chars.elements.map {
            val c = it.jvm as? Char ?: throw LispRuntimeError("'string' expected a list of chars but got element $it")
            c.toByte()
        }
        val str = String(characters.toList().toByteArray())
        StringValue(str)
    }

    private val chars = BuiltInFunction("chars", arity = 1) { (str) ->
        if (str !is StringValue) throw LispRuntimeError("'chars' expected a string")
        ListValue(SinglyLinkedList.fromList(str.jvm.toList().map { CharValue(it) }))
    }

    private inline fun <reified T, reified C> conversion(
        name: Identifier,
        cName: String,
        crossinline convert: (T) -> C
    ) =
        BuiltInFunction(name, arity = 2) { (v) ->
            val t = v.jvm as? T ?: throw RuntimeException("'name' expected $cName but got $v")
            val converted = convert(t)
            Value.of(converted)
        }

    private val c2i = conversion("c2i", "char", Char::toInt)
    private val i2c = conversion("i2c", "int", Int::toChar)
    private val i2d = conversion("i2d", "int", Int::toDouble)
    private val d2i = conversion("d2i", "double", Double::toInt)

    private fun MutableMap<Identifier, BuiltInExpr>.builtIn(func: BuiltInFunction) {
        put(func.name, BuiltInExpr(func))
    }

    private val builtIns = mutableMapOf<Identifier, BuiltInExpr>().apply {
        builtIn(let)
        builtIn(plus)
        builtIn(minus)
        builtIn(times)
        builtIn(div)
        builtIn(head)
        builtIn(tail)
        builtIn(string)
        builtIn(chars)
        builtIn(c2i)
        builtIn(i2c)
        builtIn(i2d)
        builtIn(d2i)
    }

    fun get(name: Identifier) = builtIns[name]
}