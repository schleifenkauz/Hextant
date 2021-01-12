/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.codegen

import hextant.codegen.MainProcessor.Companion.ACCESSOR_PACKAGE
import hextant.codegen.MainProcessor.Companion.GENERATED_DIR
import hextant.codegen.editor.KotlinMetadata
import krobot.api.KotlinFileRobot
import krobot.api.kotlinFile
import krobot.ast.NamedTopLevelElement
import krobot.ast.TopLevelElement
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

internal abstract class AnnotationProcessor<A : Annotation, E : Element> {
    protected lateinit var processingEnv: ProcessingEnvironment
        private set
    protected lateinit var generatedDir: String
        private set

    protected lateinit var accessorPackage: String
        private set

    protected lateinit var metadata: KotlinMetadata
        private set

    fun init(env: ProcessingEnvironment) {
        processingEnv = env
        generatedDir = env.options[GENERATED_DIR]!!
        accessorPackage = env.options[ACCESSOR_PACKAGE] ?: "hextant.generated"
        metadata = KotlinMetadata(env)
    }

    abstract fun process(element: E, annotation: A)

    protected val annotationClass = getTypeArgument(AnnotationProcessor::class, 0).java as Class<A>

    fun process(roundEnv: RoundEnvironment) {
        for (element in roundEnv.getElementsAnnotatedWith(annotationClass)) {
            val ann = element.getAnnotation(annotationClass)
            process(element as E, ann)
        }
    }

    protected fun writeKotlinFile(file: TopLevelElement, name: String) {
        file.saveToSourceRoot(generatedDir, name)
    }

    protected fun writeKotlinFile(file: NamedTopLevelElement) {
        file.saveToSourceRoot(generatedDir)
    }

    protected inline fun writeKotlinFile(name: String, body: KotlinFileRobot.() -> Unit) {
        writeKotlinFile(kotlinFile(body), name)
    }

    protected inline fun getTypeMirror(classAccessor: () -> KClass<*>): TypeMirror {
        return try {
            val cls = classAccessor()
            val name = cls.qualifiedName
            val element = processingEnv.elementUtils.getTypeElement(name)
            processingEnv.typeUtils.getDeclaredType(element)
        } catch (ex: MirroredTypeException) {
            ex.typeMirror
        }
    }

    open fun finish() {}
}