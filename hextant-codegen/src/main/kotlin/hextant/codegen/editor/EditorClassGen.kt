/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.codegen.editor

import hextant.codegen.*
import hextant.codegen.aspects.FeatureCollector
import hextant.codegen.aspects.ImplementationCollector
import krobot.api.*
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

internal abstract class EditorClassGen<A : Annotation> : AnnotationProcessor<A, TypeElement>() {
    /**
     * Looks up the annotation annotating the given [element] and delegates to [extractQualifiedEditorClassName]
     */
    private fun lookupQualifiedEditorClassName(element: Element): String {
        element.getAnnotation(UseEditor::class.java)?.let { ann ->
            val tm = getTypeMirror(ann::cls)
            return tm.toString()
        }
        val ann = getOneAnnotation(element, setOf(Token::class, Compound::class, Expandable::class))
        val suffix = if (ann is Expandable) "Expander" else "Editor"
        return extractQualifiedEditorClassName(ann, element, classNameSuffix = suffix)
    }

    protected fun extractQualifiedEditorClassName(
        ann: Annotation,
        element: Element,
        packageSuffix: String = "editor",
        classNameSuffix: String = "Editor"
    ): String {
        val configured = ann.qualifiedEditorClassName
        if (configured != null) return configured
        val pkg = processingEnv.elementUtils.getPackageOf(element)
        return "$pkg.$packageSuffix.${element.simpleName}$classNameSuffix"
    }

    protected fun KInheritanceRobot.implementEditorOfSuperType(
        annotation: Annotation,
        simpleName: String
    ) {
        val supertype = getTypeMirror { annotation.subtypeOf }.toString()
        if (supertype != None::class.qualifiedName) {
            val el = processingEnv.elementUtils.getTypeElement(supertype)
            val ann = el.getAnnotation(Alternative::class.java)
            if (ann == null) fail("No annotation of type Alternative on $supertype")
            else {
                val editorQN = extractQualifiedEditorClassName(ann, el)
                implement(editorQN.t.parameterizedBy { invariant(simpleName) })
            }
        }
    }

    protected fun getEditorClassName(tm: TypeMirror, p: VariableElement?): String {
        val useEditor = p?.getAnnotation(UseEditor::class.java)
        if (useEditor != null) {
            val cls = getTypeMirror(useEditor::cls)
            return cls.toString()
        }
        val t = checkNonPrimitive(tm)
        val e = processingEnv.typeUtils.asElement(t)
        if (e.toString() == "java.util.List") {
            val elementType = checkNonPrimitive(t.typeArguments[0]).asElement()
            val ann = elementType.getAnnotation(EditableList::class.java)
            return extractQualifiedEditorClassName(ann, elementType, classNameSuffix = "ListEditor")
        }
        return lookupQualifiedEditorClassName(e)
    }

    protected fun generatedEditor(resultType: TypeElement, clazz: String) {
        reprocess.add(Pair(resultType, clazz))
    }

    companion object {
        private val reprocess = mutableListOf<Pair<TypeElement, String>>()

        fun reprocessGenerated() {
            for ((resultType, clazz) in reprocess) {
                FeatureCollector.generatedEditor(clazz)
                ImplementationCollector.generatedEditor(resultType, clazz)
            }
            reprocess.clear()
        }
    }
}