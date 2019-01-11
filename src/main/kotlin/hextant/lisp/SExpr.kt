/**
 * @author Nikolaus Knop
 */

package hextant.lisp

typealias Identifier = String

sealed class SExpr {
    abstract val children: Iterable<SExpr>

    abstract fun evaluate(): Value

    abstract fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr
}

data class GetVal(val name: Identifier, val scope: FileScope) : SExpr() {
    override val children: Iterable<SExpr>
        get() = emptySet()

    override fun evaluate(): Value = scope.lookup(name)

    override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr) =
        if (name == identifier) replacement
        else this

    override fun toString(): String = name
}

data class Apply(val function: SExpr, val args: List<SExpr>) : SExpr() {
    override val children: Iterable<SExpr>
        get() = mutableSetOf<SExpr>().apply {
            add(function)
            addAll(args)
            addAll(function.children)
            addAll(args.flatMap { it.children })
        }

    override fun evaluate(): Value =
        function.evaluate().apply(args)

    override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr =
        Apply(
            function.replaceOccurrencesOf(identifier, replacement),
            args.map { it.replaceOccurrencesOf(identifier, replacement) }
        )

    override fun toString(): String = buildString {
        append('(')
        append("$function")
        for (arg in args) {
            append(' ')
            append("$arg")
        }
        append(')')
    }
}

data class IfExpr(val cond: SExpr, val then: SExpr, val otherwise: SExpr) : SExpr() {
    override val children: Iterable<SExpr>
        get() = setOf(cond, then, otherwise)

    override fun evaluate(): Value =
        if (cond.evaluate().toBoolean()) then.evaluate() else otherwise.evaluate()

    override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr =
        IfExpr(
            cond.replaceOccurrencesOf(identifier, replacement),
            then.replaceOccurrencesOf(identifier, replacement),
            otherwise.replaceOccurrencesOf(identifier, replacement)
        )
}

sealed class ScalarExpr : SExpr() {
    final override val children: Iterable<SExpr>
        get() = emptySet()

    final override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr = this
}

data class IntLiteral(val value: Int) : ScalarExpr() {
    override fun evaluate(): Value = IntegerValue(value)
}

data class DoubleLiteral(val value: Double) : ScalarExpr() {
    override fun evaluate(): Value = DoubleValue(value)
}

data class StringLiteral(val value: String) : ScalarExpr() {
    override fun evaluate(): Value = StringValue(value)
}

data class CharLiteral(val value: Char) : ScalarExpr() {
    override fun evaluate(): Value = CharValue(value)
}

data class LazyExpr(val expr: SExpr) : SExpr() {
    override val children: Iterable<SExpr>
        get() = expr.children + expr

    private val cached by lazy { expr.evaluate() }

    override fun evaluate(): Value = cached

    override fun replaceOccurrencesOf(identifier: Identifier, replacement: SExpr): SExpr =
        LazyExpr(expr.replaceOccurrencesOf(identifier, replacement))
}

typealias Program = Map<Identifier, Value>

fun let(bindings: List<Pair<Identifier, SExpr>>, body: SExpr) =
    Lambda(bindings.map { it.first }, body).apply(bindings.map { it.second })

infix fun SExpr.apply(arguments: List<SExpr>) = Apply(this, arguments)