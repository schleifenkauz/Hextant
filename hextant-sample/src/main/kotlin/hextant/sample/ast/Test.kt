/**
 * @author Nikolaus Knop
 */

package hextant.sample.ast

import hextant.codegen.*
import hextant.generated.eval
import hextant.plugin.Aspects


interface Term<R>

@RequestAspect
interface Eval<R, T : Term<R>> {
    fun Aspects.eval(t: T): R
}

interface I

@RequestAspect
interface Contravariant<in T : I> {
    fun Aspects.f(i: T): Int
}

@RequestAspect
interface Covariant<out T : I> {
    fun Aspects.g(i: Int): I
}

@RequestAspect
interface Invariant<T : I> {
    fun Aspects.h(i: T): Int
    fun Aspects.j(i: Int): I
}

@ProvideFeature
data class Literal(val value: String) : Term<Int>

@ProvideFeature
data class Sum(val terms: List<Term<Int>>) : Term<Int>

@ProvideImplementation
object LiteralEval : Eval<Int, Literal> {
    override fun Aspects.eval(t: Literal): Int = t.value.toInt()
}

@ProvideImplementation
object SumEval : Eval<Int, Sum> {
    override fun Aspects.eval(t: Sum): Int = t.terms.sumBy { eval(it) }
}

@ProvideProjectType("Calculator")
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