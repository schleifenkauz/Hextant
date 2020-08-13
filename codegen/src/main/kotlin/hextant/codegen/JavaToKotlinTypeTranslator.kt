package hextant.codegen

import javax.lang.model.type.*
import javax.lang.model.util.AbstractTypeVisitor6

object JavaToKotlinTypeTranslator : AbstractTypeVisitor6<String, Unit>() {
    override fun visitNoType(t: NoType, p: Unit?): String = "Unit"

    override fun visitPrimitive(t: PrimitiveType, p: Unit?): String =
        t.toString().take(1).toUpperCase() + t.toString().drop(1)

    override fun visitNull(t: NullType?, p: Unit?): String = "?"

    override fun visitArray(t: ArrayType, p: Unit?): String = "Array<${visit(t.componentType)}>"

    override fun visitDeclared(t: DeclaredType, p: Unit?): String =
        when {
            t.toString() == "java.lang.Object" -> "Any"
            t.typeArguments.isEmpty()          -> t.toString()
            else                               -> "${t.asElement()}<${t.typeArguments.joinToString(", ") { visit(it) }}>"
        }

    override fun visitError(t: ErrorType, p: Unit?): String = t.toString()

    override fun visitTypeVariable(t: TypeVariable, p: Unit?): String = t.toString()

    override fun visitWildcard(t: WildcardType, p: Unit?): String = when {
        t.extendsBound != null -> "out " + visit(t.extendsBound)
        t.superBound != null   -> "in " + visit(t.superBound)
        else                   -> "*"
    }

    override fun visitExecutable(t: ExecutableType, p: Unit?): String =
        "(${t.parameterTypes.joinToString(", ") { visit(it) }}) -> ${visit(t.returnType)}"
}