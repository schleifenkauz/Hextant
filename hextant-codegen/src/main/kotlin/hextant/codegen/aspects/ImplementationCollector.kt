/**
 *@author Nikolaus Knop
 */

package hextant.codegen.aspects

import hextant.codegen.*
import hextant.plugins.Implementation
import krobot.api.*
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.type.DeclaredType

internal object ImplementationCollector :
    AnnotationCollector<ProvideImplementation, Element, Implementation>("implementations.json") {
    override fun process(element: Element, annotation: ProvideImplementation) {
        when (element) {
            is TypeElement -> {
                val clazz = element.toString()
                element.interfaces.forEach { iface ->
                    iface as DeclaredType
                    val el = iface.asTypeElement()
                    if (el.getAnnotation(RequestAspect::class.java) != null) {
                        val aspect = el.toString()
                        val feature = iface.typeArguments.last().asTypeElement().toString()
                        add(Implementation(clazz, aspect, feature))
                    }
                }
            }
            is ExecutableElement -> {
                ensure(element.kind == CONSTRUCTOR || STATIC in element.modifiers) {
                    "Functions annotated with ProvideImplementation must be top-level"
                }
                val aspect = getTypeMirror(annotation::aspect).asTypeElement()
                val feature = getTypeMirror(annotation::feature).asTypeElement()
                val fqName = fqName(element)
                val typeParameters = element.typeParameters
                val parameters = element.parameters.map { it.toString() to toKotlinType(it.asType()) }
                generateSingleMethodImplementation(fqName, aspect, feature, typeParameters, parameters)
            }
        }
    }

    private fun generateSingleMethodImplementation(
        fqName: String,
        aspect: TypeElement,
        feature: TypeElement,
        typeParameters: List<TypeParameterElement>,
        parameters: List<Pair<String, KtType>>
    ) {
        val (pkg, simpleName) = splitPackageAndSimpleName(fqName)
        val name = "$simpleName${aspect.simpleName}"
        val impl = kotlinObject(
            pkg, name = name, modifiers = { internal() },
            inheritance = {
                val featureType = type(feature).parameterizedBy {
                    repeat(feature.typeParameters.size) { star() }
                }
                val superType = aspect.toString().t.parameterizedBy {
                    invariant(featureType)
                }
                implement(superType)
            }
        ) {
            val methods = aspect.enclosedElements.filter { it.kind == METHOD }
            val m = methods.singleOrNull() as? ExecutableElement
                ?: fail("$aspect has ${methods.size} methods, must have exactly one")
            addSingleExprFunction(
                m.simpleName.toString(),
                modifiers = { override() },
                typeParameters = copyTypeParameters(typeParameters),
                parameters = {
                    for ((n, type) in parameters) n of type
                },
            ) { call(fqName, parameters.map { (n, _) -> n.e }) }
        }
        writeKotlinFile(impl)
        add(Implementation("$pkg.$name", aspect.toString(), feature.toString()))
    }

    private fun fqName(element: ExecutableElement) =
        if (element.kind == CONSTRUCTOR) element.enclosingElement.toString()
        else "${element.enclosingElement.enclosingElement}.${element.simpleName}"

    fun generatedEditor(resultType: TypeElement, clazz: String) {
        val aspect = processingEnv.elementUtils.getTypeElement("hextant.context.EditorFactory")
        generateSingleMethodImplementation(
            clazz,
            aspect,
            resultType,
            emptyList(),
            listOf("context" to type("hextant.context.Context"))
        )
    }
}