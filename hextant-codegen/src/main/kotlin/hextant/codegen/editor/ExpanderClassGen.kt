/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.Expandable
import hextant.codegen.splitPackageAndSimpleName
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object ExpanderClassGen : EditorClassGen<Expandable>() {
    override fun process(element: TypeElement, annotation: Expandable) {
        val name = element.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "Expander")
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val (editorType, _) = getEditorInterface(element.toString(), "*")
        val delegator = getTypeMirror(annotation::delegator)
        val file = kotlinClass(
            pkg, {
                import("hextant.context.*")
                import("hextant.core.editor.*")
                import(element.toString())
                import(delegator.toString())
            },
            simpleName,
            primaryConstructor = {
                "context" of "Context"
                "editor" of editorType.nullable()
            },
            inheritance = {
                extend(
                    "Expander".t.parameterizedBy {
                        invariant(name)
                        invariant(editorType)
                    },
                    "context".e,
                    "editor".e
                )
                implementEditorOfSuperType(annotation, name)
            }
        ) {
            val delegate = delegator.toString().e call "getDelegate"
            addConstructor({ "context" of "Context" }, "context".e, "null".e)
            addConstructor(
                { "context" of "Context"; "edited" of name }, "context".e,
                "context".e.call("createEditor", "edited".e) cast editorType
            )
            addVal("config") { initializeWith(delegate) }
            addSingleExprFunction(
                "expand",
                { override() },
                parameters = { "text" of "String" }
            ) { "config".e.call("expand", "text".e, "context".e) }
            addSingleExprFunction(
                "expand",
                { override() },
                parameters = { "item" of "Any" },
            ) { "config".e.call("expand", "item".e, "context".e) }
        }
        writeKotlinFile(file)
        generatedEditor(element, "$pkg.$simpleName")
    }
}