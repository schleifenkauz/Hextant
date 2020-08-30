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
    ): Triple<String, String, KtType>? {
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
        typeParameters: List<TypeParameterElement>, parameters: List<Pair<String, KtType>>,
        fqFeatureName: String, simpleFeatureName: String, featureType: KtType,
        pkg: String, simpleName: String
    ) {
        val name = "$simpleFeatureName${aspect.simpleName}"
        val impl = kotlinObject(
            pkg, name = name, modifiers = { internal() },
            inheritance = {
                implement(aspect.toString().t.parameterizedBy {
                    invariant(featureType)
                })
            }
        ) {
            addSingleExprFunction(
                methodName,
                modifiers = { override() },
                typeParameters = copyTypeParameters(typeParameters),
                parameters = {
                    for ((n, type) in parameters) n of type
                },
            ) {
                val fqName = "$pkg.$simpleName"
                call(fqName, parameters.map { (n, _) -> n.e })
            }
        }
        writeKotlinFile(impl)
        add(Implementation("$pkg.$name", aspect.runtimeFQName(), fqFeatureName))
    }

    fun generatedEditor(resultType: TypeElement, clazz: String) {
        val aspect = processingEnv.elementUtils.getTypeElement("hextant.context.EditorFactory")
        val (pkg, simpleName) = splitPackageAndSimpleName(clazz)
        generateSingleMethodImplementation(
            aspect, "createEditor", emptyList(), listOf("context" to "hextant.context.Context".t),
            resultType.toString(), resultType.simpleName.toString(), type(resultType),
            pkg!!, simpleName
        )
    }
}