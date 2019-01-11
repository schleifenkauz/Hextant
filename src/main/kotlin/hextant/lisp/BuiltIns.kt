package hextant.lisp

object BuiltIns {
    private val let = BuiltInMacro("let", arity = 3) { (name, value, body) ->
        val getVal = name as? GetVal ?: throw LispRuntimeError("$name is no valid name")
        val ident = getVal.name
        val lambda = Lambda(listOf(ident), body)
        val applied = lambda.apply(listOf(value))
        applied
    }

    private val plus = BuiltInMacro("+", arity = 2) { (op1, op2) ->
        val operand1 = op1.evaluate().jvm
        val operand2 = op2.evaluate().jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 + operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 + operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 + operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 + operand2)
            operand1 is String && operand2 is String -> StringValue(operand1 + operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val minus = BuiltInMacro("-", arity = 2) { (op1, op2) ->
        val operand1 = op1.evaluate().jvm
        val operand2 = op2.evaluate().jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 - operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 - operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 - operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 - operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val times = BuiltInMacro("*", arity = 2) { (op1, op2) ->
        val operand1 = op1.evaluate().jvm
        val operand2 = op2.evaluate().jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 * operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 * operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 * operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 * operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val div = BuiltInMacro("/", arity = 2) { (op1, op2) ->
        val operand1 = op1.evaluate().jvm
        val operand2 = op2.evaluate().jvm
        when {
            operand1 is Double && operand2 is Double -> DoubleValue(operand1 / operand2)
            operand1 is Double && operand2 is Int    -> DoubleValue(operand1 / operand2)
            operand1 is Int && operand2 is Double    -> DoubleValue(operand1 / operand2)
            operand1 is Int && operand2 is Int       -> IntegerValue(operand1 / operand2)
            else                                     -> throw LispRuntimeError("Cannot add $operand1 and $operand2")
        }
    }

    private val escape = BuiltInMacro("`", arity = 1) { (expr) ->
        EscapedExpr(expr)
    }

    //    private val head = BuiltInFunction("head", arity = 1) { (lst) ->
    //        if (lst !is EscapedExprValue) throw LispRuntimeError("'head' expected a list but got $lst")
    //        lst.elements.head
    //    }
    //
    //    private val tail = BuiltInFunction("tail", arity = 1) { (lst) ->
    //        if (lst !is EscapedExprValue) throw LispRuntimeError("'tail' expected a list but got $lst")
    //        EscapedExprValue(lst.elements.tail)
    //    }
    //
    //    private val string = BuiltInFunction("string", arity = 1) { (chars) ->
    //        if (chars !is EscapedExprValue) throw LispRuntimeError("'string' expected a list of chars but got $chars")
    //        val characters = chars.elements.map {
    //            val c = it.jvm as? Char ?: throw LispRuntimeError("'string' expected a list of chars but got element $it")
    //            c.toByte()
    //        }
    //        val str = String(characters.toList().toByteArray())
    //        StringValue(str)
    //    }
    //
    //    private val chars = BuiltInFunction("chars", arity = 1) { (str) ->
    //        if (str !is StringValue) throw LispRuntimeError("'chars' expected a string")
    //        EscapedExprValue(SinglyLinkedList.fromList(str.jvm.toList().map { CharValue(it) }))
    //    }

    private inline fun <reified T, reified C> conversion(
        name: Identifier,
        cName: String,
        crossinline convert: (T) -> C
    ) =
        BuiltInMacro(name, arity = 2) { (v) ->
            val t = v.evaluate().jvm as? T ?: throw RuntimeException("'name' expected $cName but got $v")
            val converted = convert(t)
            Value.of(converted)
        }

    private val c2i = conversion("c2i", "char", Char::toInt)
    private val i2c = conversion("i2c", "int", Int::toChar)
    private val i2d = conversion("i2d", "int", Int::toDouble)
    private val d2i = conversion("d2i", "double", Double::toInt)

    private fun MutableMap<Identifier, BuiltInMacro>.builtIn(macro: BuiltInMacro) {
        put(macro.name, macro)
    }

    private val builtIns = mutableMapOf<Identifier, BuiltInMacro>().apply {
        builtIn(let)
        builtIn(plus)
        builtIn(minus)
        builtIn(times)
        builtIn(div)
        builtIn(escape)
        //        builtIn(head)
        //        builtIn(tail)
        //        builtIn(string)
        //        builtIn(chars)
        builtIn(c2i)
        builtIn(i2c)
        builtIn(i2d)
        builtIn(d2i)
    }

    fun get(name: Identifier) = builtIns[name]
}