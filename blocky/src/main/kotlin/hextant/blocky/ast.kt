/**
 * @author Nikolaus Knop
 */

package hextant.blocky

import hextant.blocky.editor.*
import hextant.codegen.*
import hextant.core.TokenType
import validated.*

@Token
data class Id(private val str: String) {
    override fun toString(): String = str

    companion object : TokenType<Id> {
        override fun compile(token: String): Validated<Id> = token
            .takeIf { it.all { c -> c.isLetter() } }
            .validated { invalid("Invalid identifier '$token'") }
            .map(::Id)
    }
}

@Alternative
@Expandable(ExprExpanderDelegator::class, subtypeOf = Expr::class)
sealed class Expr

@Token(subtypeOf = Expr::class)
data class IntLiteral(val value: Int) : Expr() {
    companion object : TokenType<IntLiteral> {
        override fun compile(token: String): Validated<IntLiteral> =
            token.toIntOrNull()?.let(::IntLiteral).validated { invalid("Invalid integer literal $token") }
    }
}

@Compound(subtypeOf = Expr::class)
data class Ref(val id: Id) : Expr()

@Token
enum class BinaryOperator(private val str: String) {
    Plus("+"), Minus("-"), Times("*"), Div("/"), Mod("%"),
    And("&&"), Or("||"), Xor(""),
    EQ("=="), NEQ("!="),
    LE("<="), GE(">="), LO("<"), GR(">");

    override fun toString(): String = str

    companion object : TokenType<BinaryOperator> {
        private val operators = values().associateBy { it.str }

        override fun compile(token: String): Validated<BinaryOperator> =
            operators[token].validated { invalid("Invalid binary operator $token") }
    }
}

@Compound(subtypeOf = Expr::class)
data class BinaryExpression(val op: BinaryOperator, val left: Expr, val right: Expr) : Expr()

@Token
enum class UnaryOperator(private val str: String) {
    Not("!"), Minus("-");

    override fun toString(): String = str

    companion object : TokenType<UnaryOperator> {
        private val operators = values().associateBy { it.str }

        override fun compile(token: String): Validated<UnaryOperator> =
            operators[token].validated { invalid("Invalid binary operator $token") }
    }
}

@Compound(subtypeOf = Expr::class)
data class UnaryExpression(val op: UnaryOperator, val operand: Expr) : Expr()

@Alternative
@Expandable(StatementExpanderDelegator::class, subtypeOf = Statement::class)
@EditableList
sealed class Statement

@Compound(subtypeOf = Statement::class)
data class Assign(val name: Id, val value: Expr) : Statement()

@Compound(subtypeOf = Statement::class)
data class Swap(val left: Id, val right: Id) : Statement()

@Compound(subtypeOf = Statement::class)
data class Print(val expr: Expr) : Statement()

@UseEditor(NextExecutableEditor::class)
@Alternative
sealed class Executable

@Compound(subtypeOf = Executable::class)
data class Entry(val next: Executable) : Executable()

object End : Executable()

@Compound(subtypeOf = Executable::class)
data class Block(val statements: List<Statement>, val next: Executable) : Executable()

@Compound(subtypeOf = Executable::class)
data class Branch(val condition: Expr, val yes: Executable, val no: Executable) : Executable()

@UseEditor(ProgramEditor::class)
data class Program(val start: Entry)