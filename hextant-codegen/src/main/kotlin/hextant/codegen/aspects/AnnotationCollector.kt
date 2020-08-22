/**
 *@author Nikolaus Knop
 */

package hextant.codegen.aspects

import hextant.codegen.AnnotationProcessor
import hextant.codegen.getTypeArgument
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.lang.model.element.Element
import javax.tools.StandardLocation
import kotlin.reflect.KTypeProjection.Companion.invariant
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

internal abstract class AnnotationCollector<A : Annotation, E : Element, T>(private val outputFile: String) :
    AnnotationProcessor<A, E>() {
    private val _results = mutableListOf<T>()
    protected val results: List<T> get() = _results
    private val resultType = getTypeArgument(AnnotationCollector::class, 2)


    protected fun add(result: T) {
        _results.add(result)
    }

    override fun finish() {
        if (results.isEmpty()) return
        val resource = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", outputFile)
        val listT = List::class.createType(listOf(invariant(resultType.starProjectedType)))
        val str = Json.encodeToString(serializer(listT), results)
        resource.openWriter().use { w ->
            w.write(str)
        }
        _results.clear()
    }
}