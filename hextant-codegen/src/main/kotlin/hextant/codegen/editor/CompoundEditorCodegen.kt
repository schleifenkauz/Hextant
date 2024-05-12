package hextant.codegen.editor

import hextant.codegen.*
import krobot.api.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal object CompoundEditorCodegen : EditorClassGen<Compound, Element>() {
    override fun preprocess(element: Element, annotation: Compound) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val function = extractFunction(element)
        val result = function.returnType().asTypeElement()
        if (annotation.register) {
            EditorResolution.register(result, qn) { isNodeKindNullable(annotation) || isResultNullable(function) }
        }
    }

    private fun isResultNullable(function: ExecutableElement) =
        function.parameters.any { p -> isResultNullable(p.asType(), p.getAnnotation<Component>()) }

    override fun process(element: Element, annotation: Compound) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val function = extractFunction(element)
        val resultClass = function.returnType().asTypeElement()
        val result = resultClass.simpleName.toString()
        val resultType = type(result).nullable(isNodeKindNullable(annotation) || isResultNullable(function))
        val functionName = getFunctionName(function)
        val parameters = function.parameters.map { p ->
            val ann = p.getAnnotation<Component>()
            val editorCls = getEditorClassName(p.asType(), ann)
            val ctx = ann?.childContext ?: "context"
            p.simpleName.toString() of editorCls default call(editorCls, get(ctx))
        }
        val names = function.parameters.map { p -> p.simpleName.toString() }
        classModifiers(annotation.serializable).kotlinClass(simpleName)
            .primaryConstructor(listOf("context" of "Context") + parameters)
            .extends(type("CompoundEditor", resultType), "context".e)
            .implementEditorOfSuperType(annotation, result)
            .body {
                for (name in names) {
                    `val`(name) by "child"(get(name))
                }
                override.`val`("result").of(type("ReactiveValue", resultType))
                    .initializedWith(call("composeResult", closure {
                        +call(functionName, names.map { get(it) select "now" })
                    }))
            }
            .asFile {
                `package`(pkg)
                import("hextant.core.editor.*")
                import(resultClass.toString())
                import("hextant.context.*")
                import("reaktive.value.ReactiveValue")
                import(processingEnv.fqName(element))
            }.saveToSourceRoot(generatedDir)
        if (element is TypeElement) generatedEditor(element, qn)
    }

    private fun extractFunction(element: Element) = when (element) {
        is TypeElement       -> processingEnv.elementUtils.getAllMembers(element)
            .firstOrNull { it.simpleName.toString() == "<init>" } as ExecutableElement?
            ?: fail("Class $element has no constructor")
        is ExecutableElement -> element
        else                 -> fail("Illegal annotation target for @Compound: $element")
    }
}