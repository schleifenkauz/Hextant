/**
 *@author Nikolaus Knop
 */

package hextant.codegen

import com.google.auto.service.AutoService
import hextant.plugin.Aspects
import krobot.api.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.INTERFACE
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.Modifier.*
import javax.lang.model.type.TypeMirror

@ExperimentalStdlibApi
@Suppress("unused")
@AutoService(Processor::class)
class AspectsCodegen : AbstractProcessor() {
    private lateinit var generatedDir: String

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Aspect::class.qualifiedName!!)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedOptions(): MutableSet<String> = mutableSetOf(DESTINATION)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        processingEnv.executeSafely {
            generatedDir = processingEnv.options["kapt.kotlin.generated"]!!
            val aspects = roundEnv.getElementsAnnotatedWith(Aspect::class.java).filterIsInstance<TypeElement>()
            if (aspects.isEmpty()) return@executeSafely
            val pkg = processingEnv.options[DESTINATION] ?: "hextant.generated"
            val accessors = kotlinFile(pkg) {
                generateClsFunction()
                for (aspect in aspects) {
                    generateAccessors(aspect)
                }
            }
            writeToFile(generatedDir, pkg, "aspectAccessors", accessors)
        }
        return true
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
        val caseVar =
            aspect.typeParameters.lastOrNull() ?: fail("$aspect has no type parameters, must have at least one")
        val methods = processingEnv.elementUtils.getAllMembers(aspect)
        for (m in methods) {
            if (m !is ExecutableElement) continue
            if (m.kind != METHOD) continue
            if (m.enclosingElement.kind != INTERFACE && ABSTRACT !in m.modifiers) continue
            if (PROTECTED in aspect.modifiers) continue
            generateAccessor(m, caseVar.toString(), aspect)
        }
    }

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