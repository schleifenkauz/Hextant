package hextant.codegen.aspects

import krobot.api.t
import javax.lang.model.type.*
import javax.lang.model.util.AbstractTypeVisitor9

internal object JavaToKotlinTypeTranslator : AbstractTypeVisitor9<String, Unit>() {
    override fun visitNoType(t: NoType, p: Unit?): String = "Unit"

    override fun visitPrimitive(t: PrimitiveType, p: Unit?): String =
        t.toString().take(1).uppercase() + t.toString().drop(1)

    override fun visitNull(t: NullType?, p: Unit?): String = "?"

    override fun visitArray(t: ArrayType, p: Unit?): String = "Array<${visit(t.componentType)}>"

    override fun visitDeclared(t: DeclaredType, p: Unit?): String =
        when {
            t.toString() == "java.lang.Object" -> "Any"
            t.typeArguments.isEmpty() -> t.toString()
            else -> "${t.asElement()}<${t.typeArguments.joinToString(", ") { visit(it) }}>"
        }

    override fun visitError(t: ErrorType, p: Unit?): String =
        t.toString().replace("@org.jetbrains.annotations.NotNull ", "")

    override fun visitTypeVariable(t: TypeVariable, p: Unit?): String = t.toString()

    override fun visitWildcard(t: WildcardType, p: Unit?): String = when {
        t.extendsBound != null -> "out " + visit(t.extendsBound)
        t.superBound != null -> "in " + visit(t.superBound)
        else -> "*"
    }

    override fun visitExecutable(t: ExecutableType, p: Unit?): String =
        "(${t.parameterTypes.joinToString(", ") { visit(it) }}) -> ${visit(t.returnType)}"

    override fun visitUnion(t: UnionType?, p: Unit?): String {
        throw UnsupportedOperationException("Union types are not supported")
    }

    override fun visitIntersection(t: IntersectionType?, p: Unit?): String {
        throw UnsupportedOperationException("Intersection types are not supported")
    }
}