/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.NodeType
import hextant.codegen.splitPackageAndSimpleName
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object AlternativeInterfaceCodegen : EditorClassGen<NodeType, TypeElement>() {
    override fun process(element: TypeElement, annotation: NodeType) {
        val name = element.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val typeParam = name.take(1)
        classModifiers(annotation.serializable).kotlinInterface(simpleName, out(typeParam) lowerBound name)
            .implements(type("Editor", type(typeParam).nullable(annotation.nullableResult)))
            .asFile {
                `package`(pkg)
                import("hextant.core.Editor")
                import(element.toString())
            }.saveToSourceRoot(generatedDir)
    }

}