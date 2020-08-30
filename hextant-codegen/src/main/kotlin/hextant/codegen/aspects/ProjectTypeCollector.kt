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
                val file = kotlinObject(
                    pkg, name = simpleName, modifiers = { internal() },
                    inheritance = { implement(type("hextant.project.ProjectType")) }
                ) {
                    addSingleExprFunction(
                        "createProject",
                        modifiers = { override() },
                        parameters = { "context" of "hextant.context.Context" },
                    ) {
                        val (p, n) = splitPkgAndName(element)
                        val fqName = "$p.$n"
                        call(fqName, "context".e)
                    }
                }
                writeKotlinFile(file)
                add(ProjectType(name, clazz))
            }
        }
    }
}