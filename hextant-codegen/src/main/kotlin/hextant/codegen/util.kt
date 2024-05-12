/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import hextant.codegen.aspects.JavaToKotlinTypeTranslator
import krobot.api.invariant
import krobot.api.lowerBound
import krobot.api.of
import krobot.api.type
import krobot.ast.Parameter
import krobot.ast.Type
import krobot.ast.TypeParameter
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.AnnotatedConstruct
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.*
import javax.lang.model.type.*
import javax.tools.Diagnostic
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure

internal fun fail(msg: String): Nothing {
    throw ProcessingException(msg)
}

fun ProcessingEnvironment.tryExecute(context: String, action: () -> Unit) {
    try {
        action()
    } catch (e: ProcessingException) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Error while $context: ${e.message}")
    } catch (e: Throwable) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error while $context: ${e.message}")
        e.printStackTrace()
    }
}


internal inline fun ensure(condition: Boolean, message: () -> String) {
    if (!condition) {
        val msg = message()
        fail(msg)
    }
}

internal fun Any.getTypeArgument(superclass: KClass<*>, index: Int): KClass<*> {
    val supertype = this::class.allSupertypes.find { it.classifier == superclass }!!
    val t = supertype.arguments[index].type!!
    return t.jvmErasure
}

internal inline fun <reified T : Any> fqName() = T::class.qualifiedName!!

internal fun TypeMirror.asTypeElement(): TypeElement = (this as DeclaredType).asElement() as TypeElement

internal fun allSupertypes(cls: TypeElement, visited: MutableSet<TypeElement>) {
    if (!visited.add(cls)) return
    for (iface in cls.interfaces) allSupertypes(iface.asTypeElement(), visited)
    if (cls.superclass is DeclaredType) allSupertypes(cls.superclass.asTypeElement(), visited)
}

internal fun checkNonPrimitive(t: TypeMirror): DeclaredType {
    if (t is DeclaredType) return t
    if (t is WildcardType) return checkNonPrimitive(t.extendsBound)
    fail("Invalid component type $t(${t::class})")
}

internal fun splitPackageAndSimpleName(qualifiedName: String): Pair<String?, String> {
    val idx = qualifiedName.lastIndexOf('.')
    if (idx == -1) return null to qualifiedName //No package part
    val pkg = qualifiedName.take(idx)
    val simpleName = qualifiedName.drop(idx + 1)
    return pkg to simpleName
}

internal val Annotation.qualifiedEditorClassName: String?
    get() = when (this) {
        is Token -> this.classLocation.takeIf { it != DEFAULT }
        is Compound -> this.classLocation.takeIf { it != DEFAULT }
        is NodeType -> this.interfaceLocation.takeIf { it != DEFAULT }
        is Expandable -> this.expanderLocation.takeIf { it != DEFAULT }
        is Choice -> this.classLocation.takeIf { it != DEFAULT }
        is ListEditor -> this.classLocation.takeIf { it != DEFAULT }
        else -> throw AssertionError()
    }

internal val Annotation.nodeType: KClass<*>
    get() = when (this) {
        is Token -> this.nodeType
        is Compound -> this.nodeType
        is Expandable -> this.nodeType
        is Choice -> this.nodeType
        else -> throw AssertionError()
    }

internal inline fun <E, reified F> List<E>.mapToArray(f: (E) -> F) = Array(size) { idx -> f(get(idx)) }

internal fun copyParameters(params: List<VariableElement>): List<Parameter> =
    params.map { p -> p.toString() of toKotlinType(p.asType()) }

internal fun copyTypeParameters(typeParameters: List<TypeParameterElement>): List<TypeParameter> =
    typeParameters.map { p ->
        ensure(p.bounds.size <= 1) {
            "type parameter $p of ${p.enclosingElement} has ${p.bounds.size} bounds, can have at most one"
        }
        invariant(p.toString()).lowerBound(toKotlinType(p.bounds[0]))
    }

internal fun toKotlinType(t: TypeMirror): Type {
    val str = t.accept(JavaToKotlinTypeTranslator, Unit)
    return type(str)
}

internal fun TypeMirror.supertypes(env: ProcessingEnvironment): List<TypeMirror> {
    check(this is DeclaredType)
    val e = asElement() as TypeElement
    val subst = e.typeParameters.zip(typeArguments) { p, a -> p.toString() to a }.toMap()
    val superclasses = e.interfaces + e.superclass
    return superclasses.map { t -> t.substitute(subst, env) }
}

internal fun TypeMirror.substitute(subst: Map<String, TypeMirror>, env: ProcessingEnvironment): TypeMirror =
    when (this) {
        is TypeVariable -> subst[toString()] ?: this
        is DeclaredType -> env.typeUtils.getDeclaredType(
            asElement() as TypeElement,
            *typeArguments.mapToArray { t -> t.substitute(subst, env) })

        is ArrayType -> env.typeUtils.getArrayType(componentType.substitute(subst, env))
        else -> this
    }

internal fun splitPkgAndName(element: ExecutableElement): Pair<String, String> {
    val pkg = element.enclosingElement.enclosingElement.toString()
    val simpleName =
        if (element.kind == CONSTRUCTOR) element.enclosingElement.simpleName.toString()
        else element.simpleName.toString()
    return pkg to simpleName
}

internal fun ExecutableElement.returnType(): TypeMirror =
    if (kind == CONSTRUCTOR) enclosingElement.asType() else returnType

internal fun ProcessingEnvironment.fqName(element: Element): String =
    when (element.kind) {
        CLASS -> element.toString()
        CONSTRUCTOR -> element.enclosingElement.toString()
        else -> {
            /*ensure(STATIC in element.modifiers) { "Cannot import non-static function" }*/
            val pkg = elementUtils.getPackageOf(element.enclosingElement)
            "$pkg.${element.simpleName}"
        }
    }

internal fun getFunctionName(element: ExecutableElement): String =
    if (element.kind == CONSTRUCTOR) element.enclosingElement.simpleName.toString()
    else element.simpleName.toString()


internal inline fun <reified A : Annotation> AnnotatedConstruct.getAnnotation(): A? = getAnnotation(A::class.java)

internal inline fun <reified A : Annotation> AnnotatedConstruct.hasAnnotation(): Boolean =
    getAnnotation<A>() != null

internal inline fun <reified T : Element> Element.enclosedElements(): List<T> =
    enclosedElements.filterIsInstance<T>()

internal inline fun <reified T : Element> Element.enclosedElements(kind: ElementKind): List<T> =
    enclosedElements<T>().filter { it.kind == kind }

internal fun TypeElement.getMethod(name: String): ExecutableElement? =
    enclosedElements<ExecutableElement>(METHOD).find { it.simpleName.toString() == name }

internal fun TypeElement.getEnclosedClass(name: String): TypeElement? =
    enclosedElements<TypeElement>(CLASS).find { it.simpleName.toString() == name }

internal fun TypeElement.isSubclassOf(type: String): Boolean {
    if (toString() == type) return true
    if (superclass !is NoType && superclass.asTypeElement().isSubclassOf(type)) return true
    for (iface in interfaces) {
        if (iface.asTypeElement().isSubclassOf(type)) return true
    }
    return false
}

internal fun AnnotationMirror.getValue(element: String) =
    elementValues.entries.find { (el) -> el.simpleName.toString() == element }?.value