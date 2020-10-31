/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.Alternative
import hextant.codegen.splitPackageAndSimpleName
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object AlternativeInterfaceCodegen : EditorClassGen<Alternative, TypeElement>() {
    override fun process(element: TypeElement, annotation: Alternative) {
        val name = element.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, element)
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val typeParam = name.take(1)
        val file = kotlinInterface(
            pkg, {
                import("hextant.core.Editor")
                import(element.toString())
            },
            simpleName,
            typeParameters = { covariant(typeParam, upperBound = name.t) },
            inheritance = { implement("Editor".t.parameterizedBy { invariant(typeParam) }) }
        )
        writeKotlinFile(file)
    }
}