package hextant.codegen.editor

import hextant.codegen.*
import krobot.api.*
import javax.lang.model.element.*

internal object CompoundEditorCodegen : EditorClassGen<Compound, Element>() {
    override fun preprocess(element: Element, annotation: Compound) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val function = extractFunction(element)
        val result = function.returnType().asTypeElement()
        EditorResolution.register(result, qn) { isResultNullable(function) }
    }

    private fun isResultNullable(function: ExecutableElement) =
        function.parameters.any { isResultNullable(it.asType()) }

    override fun process(element: Element, annotation: Compound) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val function = extractFunction(element)
        val resultClass = function.returnType().asTypeElement()
        val result = resultClass.simpleName.toString()
        val resultType = if (isResultNullable(function)) type(result).nullable() else type(result)
        val file = kotlinClass(
            pkg,
            imports = {
                import("hextant.core.editor.*")
                import(processingEnv.fqName(element))
                import(resultClass.toString())
                import("hextant.context.*")
                import("reaktive.value.ReactiveValue")
            },
            name = simpleName,
            primaryConstructor = { "context" of "Context" },
            inheritance = {
                extend("CompoundEditor".t.parameterizedBy { invariant(resultType) }, "context".e)
                implementEditorOfSuperType(annotation, result)
            }
        ) {
            val names = function.parameters.map { it.toString() }
            for (p in function.parameters) {
                val comp = p.getAnnotation<Component>()
                val custom = comp?.let { getTypeMirror(it::editor) }?.toString().takeIf { it != "hextant.codegen.None" }
                val editorCls = custom ?: getEditorClassName(p.asType())
                addVal(p.simpleName.toString()) {
                    by(call("child", call(editorCls, comp?.childContext?.e ?: "context".e)))
                }
            }
            addVal("result", "ReactiveValue".t.parameterizedBy { invariant(resultType) }, { override() }) {
                initializeWith(
                    call("composeResult",
                        lambda { callFunction(getFunctionName(function), *names.mapToArray { n -> n.e select "now" }) }
                    )
                )
            }
        }
        writeKotlinFile(file)
        if (element is TypeElement) generatedEditor(element, "$pkg.$simpleName")
    }

    private fun extractFunction(element: Element) = when (element) {
        is TypeElement -> processingEnv.elementUtils.getAllMembers(element)
            .firstOrNull { it.simpleName.toString() == "<init>" } as ExecutableElement?
            ?: fail("Class $element has no constructor")
        is ExecutableElement -> element
        else -> fail("Illegal annotation target for @Compound: $element")
    }
}