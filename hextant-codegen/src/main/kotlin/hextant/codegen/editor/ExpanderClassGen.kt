/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.Expandable
import hextant.codegen.asTypeElement
import hextant.codegen.editor.EditorResolution.Companion.register
import hextant.codegen.splitPackageAndSimpleName
import kotlinx.metadata.Flag
import krobot.api.*
import javax.lang.model.element.TypeElement

internal object ExpanderClassGen : EditorClassGen<Expandable, TypeElement>() {
    override fun preprocess(element: TypeElement, annotation: Expandable) {
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "Expander")
        val delegator = getTypeMirror(annotation::delegator).asTypeElement()
        val nullable = isNodeKindNullable(annotation) || hasDelegatorNullableResultType(delegator)
        register(element, qn) { nullable }
    }

    private fun hasDelegatorNullableResultType(delegator: TypeElement): Boolean {
        val delegatorSupertype =
            metadata.getSupertype(delegator, "hextant/core/editor/ExpanderDelegator") ?: return true
        val editorType = delegatorSupertype.arguments[0].type ?: return true
        val editorSupertype = metadata.getSupertype(editorType, "hextant/core/Editor") ?: return true
        val resultType = editorSupertype.arguments[0].type ?: return true
        return Flag.Type.IS_NULLABLE(resultType.flags)
    }

    override fun process(element: TypeElement, annotation: Expandable) {
        val name = element.simpleName.toString()
        val qn = extractQualifiedEditorClassName(annotation, element, classNameSuffix = "Expander")
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val (editorType, _) = getEditorInterface(element.toString(), "*")
        val delegator = getTypeMirror(annotation::delegator).asTypeElement()
        val nullableResult = hasDelegatorNullableResultType(delegator)
        kotlinClass(simpleName)
            .primaryConstructor("context" of "Context", "editor" of editorType.nullable() default `null`)
            .extends(type("Expander", type(name).nullable(nullableResult), editorType), "context".e, "editor".e)
            .implementEditorOfSuperType(annotation, name)
            .body {
                +`val`("config") initializedWith (delegator.simpleName.e call "getDelegate")
                +override.`fun`("expand", "text" of "String")
                    .returns("config".e.call("expand", "text".e, annotation.childContext.e))
                +override.`fun`("expand", "completion" of "Any")
                    .returns("config".e.call("expand", "completion".e, annotation.childContext.e))
            }
            .asFile {
                `package`(pkg)
                import("hextant.context.*")
                import("hextant.core.editor.*")
                import(element.toString())
                import(delegator.toString())
            }.saveToSourceRoot(generatedDir)
        generatedEditor(element, "$pkg.$simpleName")
    }
}