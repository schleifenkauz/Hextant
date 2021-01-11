/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.*
import kotlinx.metadata.Flag
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object ExpanderClassGen : EditorClassGen<Expandable, TypeElement>() {
    override fun preprocess(element: TypeElement, annotation: Expandable) {
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "Expander")
        val delegator = getTypeMirror(annotation::delegator).asTypeElement()
        val nullable = hasDelegatorNullableResultType(delegator)
        EditorResolution.register(element, qn, nullable)
    }

    private fun hasDelegatorNullableResultType(delegator: TypeElement): Boolean {
        val delegatorSupertype =
            metadata.getSupertype(delegator, "hextant.core.editor.ExpanderDelegator") ?: return true
        val editorType = delegatorSupertype.arguments[0].type ?: return true
        val editorSupertype = metadata.getSupertype(editorType, "hextant.core.Editor") ?: return true
        val resultType = editorSupertype.arguments[0].type ?: return true
        return Flag.Type.IS_NULLABLE(resultType.flags)
    }

    override fun process(element: TypeElement, annotation: Expandable) {
        val name = element.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "Expander")
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val (editorType, _) = getEditorInterface(element.toString(), "*")
        val delegator = getTypeMirror(annotation::delegator)
        val nullable = hasDelegatorNullableResultType(delegator.asTypeElement())
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
                        if (nullable) invariant(type(name).nullable())
                        else invariant(name)
                        invariant(editorType)
                    },
                    "context".e,
                    "editor".e
                )
                implementEditorOfSuperType(annotation, name)
            }
        ) {
            addConstructor({ "context" of "Context" }, "context".e, "null".e)
            addVal("config") { initializeWith(delegator.toString().e call "getDelegate") }
            addSingleExprFunction(
                "expand",
                { override() },
                parameters = { "text" of "String" }
            ) { "config".e.call("expand", "text".e, annotation.childContext.e) }
            addSingleExprFunction(
                "expand",
                { override() },
                parameters = { "completion" of "Any" },
            ) { "config".e.call("expand", "completion".e, annotation.childContext.e) }
        }
        writeKotlinFile(file)
        generatedEditor(element, "$pkg.$simpleName")
    }
}