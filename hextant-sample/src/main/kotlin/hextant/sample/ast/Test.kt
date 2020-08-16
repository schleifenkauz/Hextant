/**
 * @author Nikolaus Knop
 */

package hextant.sample.ast

import hextant.codegen.*
import hextant.context.Context
import hextant.core.EditorView
import hextant.core.editor.*
import hextant.core.view.TokenEditorView
import hextant.generated.eval
import hextant.plugin.Aspects
import hextant.sample.ast.Alt.O
import hextant.sample.ast.Alt.TestToken
import hextant.sample.ast.editor.*
import reaktive.value.reactiveValue
import validated.Validated
import validated.reaktive.ReactiveValidated
import validated.valid

object AltExpanderDelegator : ExpanderConfigurator<AltEditor<Alt>>({
    registerConstant("token") { TestTokenEditor(it) }
    registerConstant("comp") { CompEditor(it) }
})

class OtherTokenEditor(context: Context, t: TestToken = TestToken("Hello World")) :
    TokenEditor<TestToken, TokenEditorView>(context, t.str) {
    override fun compile(token: String): Validated<TestToken> =
        valid(TestToken("Hello World"))
}

@Alternative
@Expandable(AltExpanderDelegator::class, subtypeOf = Alt::class)
@EditableList(classLocation = "hextant.sample.editor.AltEditorList")
sealed class Alt {
    @Token(subtypeOf = Alt::class)
    data class TestToken(val str: String) : Alt() {
        companion object : TokenType<TestToken> {
            override fun compile(token: String): Validated<TestToken> =
                valid(TestToken(token))
        }
    }

    @Compound(subtypeOf = Alt::class)
    data class Comp(
        val x: Alt,
        val y: List<Alt>,
        @UseEditor(OtherTokenEditor::class) val z: TestToken,
        val o: O
    ) : Alt()

    @UseEditor(CustomEditor::class)
    object O : Alt()
}

class CustomEditor(context: Context, @Suppress("UNUSED_PARAMETER") o: O = O) : AbstractEditor<O, EditorView>(context) {
    override val result: ReactiveValidated<O> = reactiveValue(valid(O))
}


interface Term<R>

@Aspect
interface Eval<R, T : Term<R>> {
    fun Aspects.eval(t: T): R
}

interface I

@Aspect
interface Contravariant<in T : I> {
    fun Aspects.f(i: T): Int
}

@Aspect
interface Covariant<out T : I> {
    fun Aspects.g(i: Int): I
}

@Aspect
interface Invariant<T : I> {
    fun Aspects.h(i: T): Int
    fun Aspects.j(i: Int): I
}

@Feature
data class Literal(val value: String) : Term<Int>

@Feature
data class Sum(val terms: List<Term<Int>>) : Term<Int>

@Implementation
object LiteralEval : Eval<Int, Literal> {
    override fun Aspects.eval(t: Literal): Int = t.value.toInt()
}

@Implementation
object SumEval : Eval<Int, Sum> {
    override fun Aspects.eval(t: Sum): Int = t.terms.sumBy { eval(it) }
}

@ProjectType("Calculator")
class Calculator

fun main() {
    val a = Aspects()
    with(a) {
        implement(Eval::class, Literal::class, LiteralEval)
        implement(Eval::class, Sum::class, SumEval)
        val t = Sum((1..5).map { Literal("$it") })
        println(eval(t))
    }
}