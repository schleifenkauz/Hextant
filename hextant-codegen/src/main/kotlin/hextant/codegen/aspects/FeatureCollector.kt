/**
 *@author Nikolaus Knop
 */

package hextant.codegen.aspects

import hextant.codegen.ProvideFeature
import hextant.codegen.allSupertypes
import hextant.plugins.Feature
import javax.lang.model.element.TypeElement

internal object FeatureCollector : AnnotationCollector<ProvideFeature, TypeElement, Feature>("features.json") {
    override fun process(element: TypeElement, annotation: ProvideFeature) {
        val supertypes = mutableSetOf<TypeElement>()
        allSupertypes(element, supertypes)
        add(Feature(element.runtimeFQName(), supertypes.map { it.runtimeFQName() }))
    }

    fun generatedEditor(clazz: String) {
        add(Feature(clazz, listOf("hextant.core.Editor")))
    }
}