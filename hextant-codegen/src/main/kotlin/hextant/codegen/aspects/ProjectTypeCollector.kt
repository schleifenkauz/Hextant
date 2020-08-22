/**
 *@author Nikolaus Knop
 */

package hextant.codegen.aspects

import hextant.codegen.ProvideProjectType
import hextant.plugins.ProjectType
import javax.lang.model.element.TypeElement

internal object ProjectTypeCollector :
    AnnotationCollector<ProvideProjectType, TypeElement, ProjectType>("projectTypes.json") {
    override fun process(element: TypeElement, annotation: ProvideProjectType) {
        val name = annotation.name
        val clazz = element.toString()
        add(ProjectType(name, clazz))
    }
}