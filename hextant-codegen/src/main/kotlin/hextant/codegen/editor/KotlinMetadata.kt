/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.getAnnotation
import kotlinx.metadata.*
import kotlinx.metadata.KmClassifier.TypeParameter
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.KotlinClassMetadata.Class
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class KotlinMetadata(private val processingEnv: ProcessingEnvironment) {
    private fun kotlinMetadata(element: TypeElement): KmClass? {
        val metadata = element.getAnnotation<Metadata>() ?: return null
        val header = KotlinClassHeader(
            metadata.kind,
            metadata.metadataVersion,
            metadata.bytecodeVersion,
            metadata.data1,
            metadata.data2,
            metadata.extraString,
            metadata.packageName,
            metadata.extraInt
        )
        val clazz = KotlinClassMetadata.read(header) as Class
        return clazz.toKmClass()
    }

    private fun kotlinMetadata(className: ClassName): KmClass? {
        val canonical = className.replace('/', '.')
        val element = processingEnv.elementUtils.getTypeElement(canonical) ?: return null
        return kotlinMetadata(element)
    }

    private fun KmType.substitute(subst: Map<Int, KmType>): KmType {
        val t = KmType(flags)
        val c = classifier
        if (c is TypeParameter) return subst[c.id] ?: this
        t.classifier = c
        t.arguments.addAll(arguments.map { it.copy(type = it.type?.substitute(subst)) })
        return t
    }

    private fun KmClass.getSupertype(subst: Map<Int, KmType>, name: String): KmType? {
        for (t in supertypes) {
            val clazz = t.classifier as? KmClassifier.Class ?: continue
            if (clazz.name == name) return t.substitute(subst)
            val klass = kotlinMetadata(clazz.name) ?: continue
            val s = klass.typeParameters.zip(t.substitute(subst).arguments).associate { (p, a) -> p.id to a.type!! }
            val supertype = klass.getSupertype(s, name)
            if (supertype != null) return supertype
        }
        return null
    }

    fun getSupertype(element: TypeElement, name: String): KmType? {
        val klass = kotlinMetadata(element) ?: return null
        return klass.getSupertype(emptyMap(), name)
    }

    fun getSupertype(type: KmType, name: String): KmType? {
        val classifier = type.classifier as? KmClassifier.Class ?: return null
        val clazz = kotlinMetadata(classifier.name) ?: return null
        return clazz.getSupertype(emptyMap(), name)
    }
}