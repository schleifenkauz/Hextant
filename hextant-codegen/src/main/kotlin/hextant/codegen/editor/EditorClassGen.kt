/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.codegen.editor

import hextant.codegen.*
import hextant.codegen.aspects.FeatureCollector
import hextant.codegen.aspects.ImplementationCollector
import kotlinx.metadata.Flag
import krobot.api.call
import krobot.api.e
import krobot.api.implements
import krobot.api.type
import krobot.ast.CanImplement
import krobot.ast.ClassDefinition
import krobot.ast.Type
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

internal abstract class EditorClassGen<A : Annotation, E : Element> : AnnotationProcessor<A, E>() {
    protected open fun preprocess(element: E, annotation: A) {}

    fun preprocess(roundEnv: RoundEnvironment) {
        for (element in roundEnv.getElementsAnnotatedWith(annotationClass)) {
            val ann = element.getAnnotation(annotationClass)
            preprocess(element as E, ann)
        }
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
        return if (element.kind == CONSTRUCTOR) {
            extractQualifiedEditorClassName(ann, element.enclosingElement, packageSuffix, classNameSuffix)
        } else {
            val capitalized = element.simpleName.toString().capitalize()
            "$pkg.$packageSuffix.$capitalized$classNameSuffix"
        }
    }

    protected fun <C: ClassDefinition<CanImplement>> C.implementEditorOfSuperType(
        annotation: Annotation,
        simpleName: String
    ): C = apply {
        val supertype = getTypeMirror(annotation::nodeType).toString()
        if (supertype != None::class.qualifiedName) {
            val (t, delegated) = getEditorInterface(supertype, simpleName)
            implements(t)
            for (iface in delegated) {
                implements(type(iface.toString()), iface.e call "delegate()")
            }
        }
    }

    private fun hasEditorNullableResultType(element: TypeElement): Boolean {
        val supertype = metadata.getSupertype(element, "hextant/core/Editor") ?: return true
        val resultType = supertype.arguments[0].type ?: return true
        return Flag.Type.IS_NULLABLE(resultType.flags)
    }

    protected fun isNodeKindNullable(annotation: Annotation): Boolean {
        val supertype = getTypeMirror(annotation::nodeType).toString()
        if (supertype == None::class.qualifiedName) return false
        val el = processingEnv.elementUtils.getTypeElement(supertype)
        val generated = el.getAnnotation<Alternative>()
        if (generated != null) return generated.nullableResult
        val linked = el.getAnnotation<EditorInterface>()
        if (linked != null) {
            val tm = getTypeMirror(linked::clz)
            return hasEditorNullableResultType(tm.asTypeElement())
        }
        return false
    }

    protected fun getEditorInterface(type: String, concreteType: String): Pair<Type, List<TypeMirror>> {
        val el = processingEnv.elementUtils.getTypeElement(type)
        val generated = el.getAnnotation<Alternative>()
        val linked = el.getAnnotation<EditorInterface>()
        return when {
            generated == null && linked == null -> fail("Can't find common editor interface for $type")
            generated != null && linked != null -> fail("Conflicting annotations on $type")
            generated != null                   -> {
                val editorQN = extractQualifiedEditorClassName(generated, el)
                type(editorQN, concreteType) to emptyList()
            }
            linked != null                      -> {
                val t = getTypeMirror(linked::clz)
                val delegated = try {
                    linked.delegated
                        .map { c -> processingEnv.elementUtils.getTypeElement(c.qualifiedName) }
                        .map { e -> processingEnv.typeUtils.getDeclaredType(e) }
                } catch (ex: MirroredTypesException) {
                    ex.typeMirrors
                }
                type(t.toString(), concreteType) to delegated
            }
            else                                -> fail("impossible")
        }
    }

    private fun editorResolution(editorClass: () -> KClass<*>): EditorResolution {
        val element = getTypeMirror(editorClass).asTypeElement()
        val nullable = hasEditorNullableResultType(element)
        return EditorResolution(element.toString(), nullable)
    }

    private fun resolveEditor(type: TypeMirror, annotation: Component?): EditorResolution {
        if (annotation != null && getTypeMirror(annotation::editor).toString() != None::class.qualifiedName)
            return editorResolution(annotation::editor)
        val t = checkNonPrimitive(type)
        val clazz = t.asTypeElement()
        val custom = clazz.getAnnotation<UseEditor>()
        if (custom != null) return editorResolution(custom::cls)
        return if (clazz.toString() == "java.util.List") {
            val elementType = checkNonPrimitive(t.typeArguments[0]).asTypeElement()
            val ann = elementType.getAnnotation<EditableList>() ?: fail("Could not locate editor class for type $t")
            val qn = extractQualifiedEditorClassName(ann, elementType, classNameSuffix = "ListEditor")
            return EditorResolution(qn) { false }
        } else {
            EditorResolution.resolve(clazz) ?: fail("Could not locate editor class for type $t")
        }
    }

    protected fun getEditorClassName(type: TypeMirror, annotation: Component? = null): String =
        resolveEditor(type, annotation).className

    protected fun isResultNullable(type: TypeMirror, annotation: Component? = null): Boolean =
        resolveEditor(type, annotation).isResultNullable

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