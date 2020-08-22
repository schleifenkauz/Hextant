/**
 * @author Nikolaus Knop
 */

@file:Suppress("UNCHECKED_CAST")

package hextant.codegen

import hextant.codegen.MainProcessor.Companion.ACCESSOR_PACKAGE
import hextant.codegen.MainProcessor.Companion.GENERATED_DIR
import krobot.api.KotlinFile
import krobot.api.writeTo
import java.nio.file.Files
import java.nio.file.Paths
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

    protected lateinit var accessorPackage: String
        private set


    protected open fun init(env: ProcessingEnvironment) {
        processingEnv = env
        generatedDir = env.options[GENERATED_DIR]!!
        accessorPackage = env.options[ACCESSOR_PACKAGE] ?: "hextant.generated"
    }

    abstract fun process(element: E, annotation: A)

    private val annotationClass = getTypeArgument(AnnotationProcessor::class, 0).java as Class<A>

    fun process(env: ProcessingEnvironment, roundEnv: RoundEnvironment) {
        init(env)
        for (element in roundEnv.getElementsAnnotatedWith(annotationClass)) {
            val ann = element.getAnnotation(annotationClass)
            process(element as E, ann)
        }
    }

    protected fun writeKotlinFile(file: KotlinFile) {
        val packages = file.pkg?.split('.')?.toTypedArray() ?: emptyArray()
        val path = Paths.get(generatedDir, *packages, file.name)
        Files.createDirectories(path.parent)
        file.writeTo(path)
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