/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.Token
import hextant.codegen.splitPackageAndSimpleName
import krobot.api.*
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

internal object TokenEditorCodegen : EditorClassGen<Token, Element>() {
    override fun process(element: Element, annotation: Token) {
        if (element is TypeElement) {
            val name = element.simpleName.toString()
            val qn = extractQualifiedEditorClassName(annotation, element)
            val (pkg, simpleName) = splitPackageAndSimpleName(qn)
            val file = kotlinClass(
                pkg,
                {
                    import("hextant.context.Context")
                    import("hextant.core.editor.*")
                    import("hextant.core.view.*")
                    import(element.toString())
                },
                simpleName,
                primaryConstructor = { "context" of "Context"; "text" of type("String") },
                inheritance = {
                    extend(
                        "TokenEditor".t.parameterizedBy { invariant(name); invariant("TokenEditorView") },
                        "context".e,
                        "text".e
                    )
                    implementEditorOfSuperType(annotation, name)
                }
            ) {
                addConstructor({ "context" of "Context" }, "context".e, stringLiteral(""))
                addConstructor({
                    "context" of "Context"
                    "value" of name
                }, "context".e, "value".e call "toString")
                addSingleExprFunction(
                    "compile",
                    { override() },
                    parameters = { "token" of "String" }) { name.e.call("compile", "token".e) }
            }
            writeKotlinFile(file)
            generatedEditor(element, "$pkg.$simpleName")
        }
    }
}