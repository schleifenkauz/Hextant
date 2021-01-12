/**
 *@author Nikolaus Knop
 */

package hextant.codegen.aspects

import hextant.codegen.*
import hextant.plugins.ProjectType
import krobot.api.*
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.Modifier.STATIC

internal object ProjectTypeCollector :
    AnnotationCollector<ProvideProjectType, Element, ProjectType>("projectTypes.json") {
    override fun process(element: Element, annotation: ProvideProjectType) {
        val name = annotation.name
        when (element) {
            is TypeElement -> {
                val clazz = element.runtimeFQName()
                add(ProjectType(name, clazz))
            }
            is ExecutableElement -> {
                ensure(element.kind == CONSTRUCTOR || STATIC in element.modifiers) {
                    "Functions annotated with ProvideImplementation must be top-level"
                }
                val type = element.returnType()
                val clazz = "${type}Factory"
                val (pkg, simpleName) = splitPackageAndSimpleName(clazz)
                internal.kotlinObject(simpleName).implements("hextant.project.ProjectType").body {
                    val (p, n) = splitPkgAndName(element)
                    override.`fun`("createProject", "context" of "hextant.context.Context")
                        .returns(call("$p.$n", get("context")))
                }.asFile {
                    `package`(pkg)
                }.saveToSourceRoot(generatedDir)
                add(ProjectType(name, clazz))
            }
        }
    }
}