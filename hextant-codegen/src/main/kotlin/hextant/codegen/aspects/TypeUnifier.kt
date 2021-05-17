package hextant.codegen.aspects

import hextant.codegen.asTypeElement
import hextant.codegen.ensure
import hextant.codegen.supertypes
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.*

internal class TypeUnifier(private val env: ProcessingEnvironment) {
    private val subst = mutableMapOf<String, TypeMirror>()

    private fun doUnify(expected: TypeMirror, provided: TypeMirror): Boolean {
        var possible = true
        when {
            expected is WildcardType                             -> {
            }
            expected is TypeVariable                             -> subst[expected.toString()] = provided
            expected is DeclaredType && provided is DeclaredType -> {
                possible =
                    if (expected.asTypeElement() == provided.asTypeElement())
                        expected.typeArguments.zip(provided.typeArguments).all { (a, b) -> doUnify(a, b) }
                    else
                        provided.supertypes(env).any { sub -> doUnify(expected, sub) }
            }
            expected is ArrayType && provided is ArrayType       -> possible =
                doUnify(expected.componentType, provided.componentType)
            else                                                 -> possible = false
        }
        return possible
    }

    fun unify(expected: TypeMirror, provided: TypeMirror) {
        ensure(doUnify(expected, provided)) { "Cannot unify $provided <: $expected" }
    }

    fun lookup(typeVar: String): TypeMirror? = subst[typeVar]
}