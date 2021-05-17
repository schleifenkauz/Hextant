/**
 *@author Nikolaus Knop
 */

package hextant.codegen.aspects

import hextant.codegen.*
import hextant.plugins.Implementation
import krobot.api.*
import krobot.ast.Type
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.DeclaredType

internal object ImplementationCollector :
    AnnotationCollector<ProvideImplementation, Element, Implementation>("implementations.json") {
    override fun process(element: Element, annotation: ProvideImplementation) {
        when (element) {
            is TypeElement -> {
                val clazz = element.runtimeFQName()
                element.interfaces.forEach { iface ->
                    iface as DeclaredType
                    val el = iface.asTypeElement()
                    if (el.getAnnotation(RequestAspect::class.java) != null) {
                        val aspect = el.runtimeFQName()
                        val feature = iface.typeArguments.last().asTypeElement().runtimeFQName()
                        add(Implementation(clazz, aspect, feature))
                    }
                }
            }
            is ExecutableElement -> {
                ensure(element.kind == CONSTRUCTOR || STATIC in element.modifiers) {
                    "Functions annotated with ProvideImplementation must be top-level"
                }
                val aspect = getTypeMirror(annotation::aspect).asTypeElement()
                val typeVar = aspect.typeParameters.last().toString()
                val methods = aspect.enclosedElements.filter { it.kind == METHOD }
                val decl = methods.singleOrNull() as? ExecutableElement
                    ?: fail("$aspect has ${methods.size} methods, must have exactly one")
                val (fqFeatureName, simpleFeatureName, featureType) = inferFeature(decl, element, typeVar) ?: return
                val (pkg, simpleName) = splitPkgAndName(element)
                val typeParameters = element.typeParameters
                val parameters = element.parameters.map { it.toString() to toKotlinType(it.asType()) }
                generateSingleMethodImplementation(
                    aspect, decl.simpleName.toString(), typeParameters, parameters,
                    fqFeatureName, simpleFeatureName, featureType,
                    pkg, simpleName
                )
            }
        }
    }

    private fun inferFeature(
        decl: ExecutableElement,
        impl: ExecutableElement,
        aspectTypeVar: String
    ): Triple<String, String, Type>? {
        val unifier = TypeUnifier(processingEnv)
        for ((a, b) in decl.parameters.zip(impl.parameters)) {
            unifier.unify(a.asType(), b.asType())
        }
        val ret = impl.returnType()
        unifier.unify(decl.returnType, ret)
        val featureType = unifier.lookup(aspectTypeVar)
            ?: error("Cannot infer feature type for $impl from method signature")
        if (featureType.toString() == "error.NonExistentClass") return null
        check(featureType is DeclaredType) { "Invalid feature type $featureType" }
        val feature = featureType.asTypeElement()
        val featureName = feature.simpleName.toString()
        return Triple(feature.runtimeFQName(), featureName, toKotlinType(featureType))
    }

    private fun generateSingleMethodImplementation(
        aspect: TypeElement, methodName: String,
        typeParameters: List<TypeParameterElement>, parameters: List<Pair<String, Type>>,
        fqFeatureName: String, simpleFeatureName: String, featureType: Type,
        pkg: String, simpleName: String
    ) {
        val name = "$simpleFeatureName${aspect.simpleName}"
        internal.kotlinObject(name).implements(type(aspect.toString(), featureType)).body {
            +override.`fun`(copyTypeParameters(typeParameters), methodName)
                .parameters(parameters.map { (n, t) -> n of t })
                .returns(call("$pkg.$simpleName", parameters.map { (n, _) -> get(n) }))
        }.asFile {
            `package`(pkg)
        }.saveToSourceRoot(generatedDir)
        add(Implementation("$pkg.$name", aspect.runtimeFQName(), fqFeatureName))
    }

    fun generatedEditor(resultType: TypeElement, clazz: String) {
        val aspect = processingEnv.elementUtils.getTypeElement("hextant.context.EditorFactory")
        val (pkg, simpleName) = splitPackageAndSimpleName(clazz)
        generateSingleMethodImplementation(
            aspect, "createEditor", emptyList(), listOf("context" to type("hextant.context.Context")),
            resultType.toString(), resultType.simpleName.toString(), type(resultType.toString()),
            pkg!!, simpleName
        )
    }
}