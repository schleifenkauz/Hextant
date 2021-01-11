/**
 *@author Nikolaus Knop
 */

package hextant.codegen.aspects

import hextant.codegen.*
import hextant.plugins.Aspect
import krobot.api.*
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.INTERFACE
import javax.lang.model.element.ElementKind.METHOD
import javax.lang.model.element.Modifier.*

internal object AspectCollector : AnnotationCollector<RequestAspect, TypeElement, Aspect>("aspects.json") {
    override fun process(element: TypeElement, annotation: RequestAspect) {
        val target = caseVar(element).bounds.firstOrNull()?.asTypeElement()?.runtimeFQName()
            ?: throw ProcessingException("Cannot deduce target of aspect $element")
        add(Aspect(element.runtimeFQName(), target, annotation.optional))
    }

    override fun finish() {
        if (results.isEmpty()) return
        val accessors = kotlinFile(accessorPackage, "aspectAccessors.kt") {
            generateClsFunction()
            for ((clazz, _) in results) {
                val element = processingEnv.elementUtils.getTypeElement(clazz.replace('$', '.'))
                generateAccessors(element)
            }
        }
        writeKotlinFile(accessors)
        super.finish()
    }

    private fun KFileRobot.generateClsFunction() {
        addSingleExprFunction(
            "cls",
            modifiers = {
                private()
                inline()
            },
            typeParameters = { invariant("reified T", type("Any")) }) {
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

    private fun KFileRobot.generateAccessor(m: ExecutableElement, caseVar: String, aspect: TypeElement) {
        val caseParam = m.parameters.find { it.asType().toString() == caseVar }?.toString()
        val name = m.simpleName.toString()
        val params = m.parameters.filter { '$' !in it.toString() }
        addSingleExprFunction(
            name,
            modifiers = {
                if (PUBLIC !in m.modifiers) internal()
            },
            receiver = type("hextant.plugin.Aspects"),
            typeParameters = {
                copyTypeParameters(aspect.typeParameters)()
                copyTypeParameters(m.typeParameters)()
            },
            parameters = {
                if (caseParam == null) {
                    "case" of type("kotlin.reflect.KClass").parameterizedBy { invariant(caseVar) }
                }
                copyParameters(params)()
            }
        ) {
            val case = caseParam?.let { ("$it::class").e } ?: "case".e
            val impl = "get"("cls<${aspect.asType()}>()".e, case)
            val args = params.map { it.toString().e }
            impl.call(name, args)
            if (m.parameters.size != params.size)
                "with"(impl, lambda {
                    evaluate("with"("this@$name".e, lambda {
                        evaluate(call(name, args))
                    }))
                })
            else impl.call(name, args)
        }
    }


    private fun checkAbstract(aspect: TypeElement) {
        val isInterface = aspect.kind == INTERFACE
        ensure(isInterface || ABSTRACT in aspect.modifiers) { "concrete class $aspect cannot be an aspect" }
    }
}