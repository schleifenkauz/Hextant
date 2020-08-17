/**
 *@author Nikolaus Knop
 */

package hextant.codegen

import com.google.auto.service.AutoService
import hextant.plugin.Aspects
import hextant.plugins.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import krobot.api.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.INTERFACE
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.Modifier.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.StandardLocation

@ExperimentalStdlibApi
@Suppress("unused")
@AutoService(Processor::class)
class AspectsCodegen : AbstractProcessor() {
    private lateinit var generatedDir: String

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(RequestAspect::class.qualifiedName!!)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedOptions(): MutableSet<String> = mutableSetOf(DESTINATION)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        processingEnv.executeSafely {
            generatedDir = processingEnv.options["kapt.kotlin.generated"]!!
            val pkg = processingEnv.options[DESTINATION] ?: "hextant.generated"
            with(roundEnv) {
                visitAspects(pkg)
                visitFeatures()
                visitImplementations()
                visitProjectTypes()
            }
        }
        return true
    }

    private fun TypeMirror.asTypeElement(): TypeElement = (this as DeclaredType).asElement() as TypeElement

    private fun allSupertypes(cls: TypeElement, visited: MutableSet<TypeElement>) {
        if (!visited.add(cls)) return
        for (iface in cls.interfaces) allSupertypes(iface.asTypeElement(), visited)
        if (cls.superclass is DeclaredType) allSupertypes(cls.superclass.asTypeElement(), visited)
    }

    private fun RoundEnvironment.visitFeatures() {
        collect("features") { cls, _: RequestFeature ->
            val supertypes = mutableSetOf<TypeElement>()
            allSupertypes(cls, supertypes)
            listOf(Feature(cls.toString(), supertypes.map { it.toString() }))
        }
    }

    private fun RoundEnvironment.visitImplementations() {
        collect("implementations") { cls, _: ProvideImplementation ->
            val clazz = cls.toString()
            cls.interfaces.mapNotNull { iface ->
                iface as DeclaredType
                val el = iface.asTypeElement()
                if (el.getAnnotation(RequestAspect::class.java) != null) {
                    val aspect = el.toString()
                    val feature = iface.typeArguments.last().asTypeElement().toString()
                    Implementation(clazz, aspect, feature)
                } else null
            }
        }
    }

    private fun RoundEnvironment.visitProjectTypes() {
        collect("projectTypes") { cls, ann: ProvideProjectType ->
            val name = ann.name
            val clazz = cls.toString()
            listOf(ProjectType(name, clazz))
        }
    }

    private inline fun <reified A : Annotation, reified T> RoundEnvironment.collect(
        name: String,
        extract: (TypeElement, A) -> Iterable<T>
    ) {
        val classes = getElementsAnnotatedWith(A::class.java).filterIsInstance<TypeElement>()
        if (classes.isEmpty()) return
        val extracted = classes.flatMap { cls ->
            val ann = cls.getAnnotation(A::class.java)
            extract(cls, ann)
        }
        writeJson(extracted, "$name.json")
    }

    private inline fun <reified T> writeJson(list: List<T>, file: String) {
        val resource = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", file)
        val str = Json.encodeToString(serializer(), list)
        val w = resource.openWriter()
        w.write(str)
        w.close()
    }

    private fun RoundEnvironment.visitAspects(pkg: String) {
        collect("aspects") { cls, ann: RequestAspect ->
            val name = cls.toString()
            val target = caseVar(cls).bounds.firstOrNull()?.asTypeElement()?.toString()
                ?: throw ProcessingException("Cannot deduce target of aspect $cls")
            listOf(Aspect(name, target, ann.optional))
        }

        val classes = getElementsAnnotatedWith(RequestAspect::class.java).filterIsInstance<TypeElement>()
        if (classes.isEmpty()) return
        val accessors = kotlinFile(pkg) {
            generateClsFunction()
            for (cls in classes) {
                generateAccessors(cls)
            }
        }
        writeToFile(generatedDir, pkg, "aspectAccessors", accessors)
    }

    private fun KFileRobot.generateClsFunction() {
        addSingleExprFunction(
            "cls",
            modifiers = {
                private()
                inline()
            },
            typeParameters = { invariant("reified T", type<Any>()) }) {
            "T::class".e
        }
    }

    private fun KFileRobot.generateAccessors(aspect: TypeElement) {
        checkAbstract(aspect)
        val methods = processingEnv.elementUtils.getAllMembers(aspect)
        for (m in methods) {
            if (m !is ExecutableElement) continue
            if (m.kind != METHOD) continue
            if (m.enclosingElement.kind != INTERFACE && ABSTRACT !in m.modifiers) continue
            if (PROTECTED in aspect.modifiers) continue
            generateAccessor(m, caseVar(aspect).toString(), aspect)
        }
    }

    private fun caseVar(aspect: TypeElement): TypeParameterElement = aspect.typeParameters.lastOrNull()
        ?: fail("$aspect has no type parameters, must have at least one")

    private fun kotlin(t: TypeMirror): KtType {
        val str = t.accept(JavaToKotlinTypeTranslator, Unit)
        return type(str)
    }

    private fun KFileRobot.generateAccessor(m: ExecutableElement, caseVar: String, aspect: TypeElement) {
        val caseParam = m.parameters.getOrNull(1)?.takeIf { it.asType().toString() == caseVar }?.toString()
        val name = m.simpleName.toString()
        val params = m.parameters.filter { '$' !in it.toString() }
        addSingleExprFunction(
            name,
            modifiers = {
                if (PUBLIC !in m.modifiers) internal()
            },
            receiver = type<Aspects>(),
            typeParameters = {
                copyTypeParameters(aspect.typeParameters, aspect)
                copyTypeParameters(m.typeParameters, m)
            },
            parameters = {
                if (caseParam == null) {
                    "case" of type("kotlin.reflect.KClass").parameterizedBy { invariant(caseVar) }
                }
                for (p in params) {
                    p.toString() of kotlin(p.asType())
                }
            },
            returnType = kotlin(m.returnType)
        ) {
            val case = caseParam?.let { ("$it::class").e } ?: "case".e
            val impl = "get"("cls<${aspect.asType()}>()".e, case)
            val args = params.map { it.toString().e }
            "with"(impl, lambda {
                evaluate("with"("this@$name".e, lambda {
                    evaluate(call(name, args))
                }))
            })
        }
    }

    private fun KTypeParametersRobot.copyTypeParameters(
        typeParameters: List<TypeParameterElement>,
        element: Element
    ) {
        for (p in typeParameters) {
            ensure(p.bounds.size <= 1) { "type parameter $p of $element has ${p.bounds.size} bounds, can have at most one" }
            invariant(p.toString(), kotlin(p.bounds[0]))
        }
    }

    private fun ensureAtMostOneBound(p: TypeParameterElement, m: Element) {
        ensure(p.bounds.size <= 1) { "type parameter $p of $m has ${p.bounds.size} bounds, can have at most one" }
    }

    private fun checkAbstract(aspect: TypeElement) {
        val isInterface = aspect.kind == INTERFACE
        ensure(isInterface || ABSTRACT in aspect.modifiers) { "concrete class $aspect cannot be an aspect" }
    }

    companion object {
        private const val DESTINATION = "hextant.codegen.dest"
    }
}