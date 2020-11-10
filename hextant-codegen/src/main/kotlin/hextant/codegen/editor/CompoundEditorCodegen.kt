package hextant.codegen.editor

import hextant.codegen.*
import krobot.api.*
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.CONSTRUCTOR

internal object CompoundEditorCodegen : EditorClassGen<Compound, Element>() {
    override fun process(element: Element, annotation: Compound) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val function = when (element) {
            is TypeElement -> processingEnv.elementUtils.getAllMembers(element)
                .firstOrNull { it.simpleName.toString() == "<init>" } as ExecutableElement?
                ?: fail("Class $element has no constructor")
            is ExecutableElement -> element
            else -> fail("Illegal annotation target for @Compound: $element")
        }
        val result =
            if (function.kind == CONSTRUCTOR) function.enclosingElement.simpleName.toString()
            else function.returnType.asTypeElement().simpleName.toString()
        val file = kotlinClass(
            pkg,
            imports = {
                import("hextant.core.editor.*")
                if (element is TypeElement) import(element.toString())
                else if (element is ExecutableElement) {
                    if (element.kind == CONSTRUCTOR) import(element.enclosingElement.toString())
                    else {
                        import(element.returnType.asTypeElement().toString())
                        val p = processingEnv.elementUtils.getPackageOf(element)
                        import("$p.${element.simpleName}")
                    }
                }
                import("hextant.context.*")
                import("reaktive.value.*")
            },
            name = simpleName,
            primaryConstructor = { "context" of "Context" },
            inheritance = {
                extend("CompoundEditor".t.parameterizedBy { invariant(result) }, "context".e)
                implementEditorOfSuperType(annotation, result)
            }
        ) {
            val names = function.parameters.map { it.toString() }
            for (p in function.parameters) {
                val comp = p?.getAnnotation(Component::class.java)
                val custom = comp?.let { getTypeMirror { it.editor } }?.toString()
                    .takeIf { it != "hextant.codegen.None" }
                val editorCls = custom ?: getEditorClassName(p.asType())
                addVal(p.simpleName.toString()) {
                    by(call("child", call(editorCls, comp?.childContext?.e ?: "context".e)))
                }
            }
            val type = "ReactiveValue".t.parameterizedBy { invariant("$result?") }
            addVal("result", type, { override() }) {
                val fct =
                    if (function.kind == CONSTRUCTOR) function.enclosingElement.simpleName.toString()
                    else function.simpleName.toString()
                initializeWith(
                    call("composeResult",
                        lambda { callFunction(fct, *names.mapToArray { n -> n.e call "get" }) }
                    )
                )
            }
        }
        writeKotlinFile(file)
        if (element is TypeElement) generatedEditor(element, "$pkg.$simpleName")
    }
}