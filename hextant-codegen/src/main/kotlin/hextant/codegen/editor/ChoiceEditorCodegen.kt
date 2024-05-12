package hextant.codegen.editor

import hextant.codegen.*
import krobot.api.*
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

internal object ChoiceEditorCodegen : EditorClassGen<Choice, Element>() {
    override fun preprocess(element: Element, annotation: Choice) {
        EditorResolution.register(nodeType(element), extractQualifiedEditorClassName(annotation, element)) { false }
    }

    override fun process(element: Element, annotation: Choice) {
        val nodeType = nodeType(element)
        val choicesFunc = choicesFunc(element)
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        classModifiers(annotation.serializable).kotlinClass(simpleName)
            .primaryConstructor(
                "context" of "Context",
                "default" of nodeType.simpleName.toString() default annotation.defaultValue.e
            )
            .extends(type("SimpleChoiceEditor", nodeType.simpleName.toString()), "context".e, "default".e)
            .implementEditorOfSuperType(annotation, nodeType.simpleName.toString())
            .body {
                +override.`fun`("choices") returns choicesFunc.e
            }
            .asFile {
                `package`(pkg)
                import("hextant.core.editor.*")
                import("hextant.context.*")
                import(nodeType)
                import(processingEnv.fqName(element))
            }
            .saveToSourceRoot(generatedDir)
        generatedEditor(nodeType, qn)
    }

    private fun nodeType(element: Element) = when (element) {
        is TypeElement -> element
        is ExecutableElement -> element.returnType.asTypeElement()
        else -> throw ProcessingException("annotation @Choice applied to invalid element $element")
    }

    private fun choicesFunc(element: Element) = when (element) {
        is TypeElement -> "${element.simpleName}.values().asList()"
        is ExecutableElement -> "${element.simpleName}()"
        else -> throw ProcessingException("annotation @Choice applied to invalid element $element")
    }
}