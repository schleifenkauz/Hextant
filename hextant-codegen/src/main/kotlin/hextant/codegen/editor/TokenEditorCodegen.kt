/**
 *@author Nikolaus Knop
 */

package hextant.codegen.editor

import hextant.codegen.*
import hextant.codegen.editor.TokenEditorCodegen.Input.*
import hextant.codegen.editor.TokenEditorCodegen.Input.Function
import krobot.api.*
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.*
import javax.lang.model.element.ElementKind.CONSTRUCTOR
import javax.lang.model.element.ElementKind.METHOD

internal object TokenEditorCodegen : EditorClassGen<Token, Element>() {
    override fun preprocess(element: Element, annotation: Token) {
        val input = getInput(element)
        val className = extractQualifiedEditorClassName(annotation, element)
        val resultType = input.getResultType()
        val resultNullable = input.isResultNullable()
        EditorResolution.register(resultType, className, resultNullable)
    }

    private sealed class Input {
        data class Function(val func: ExecutableElement) : Input()
        data class Constructor(val clazz: TypeElement) : Input()
        data class CompanionObject(val clazz: TypeElement, val func: ExecutableElement) : Input()
    }

    private fun getInput(element: Element): Input = when (element) {
        is ExecutableElement -> {
            when (element.kind) {
                METHOD      -> Function(element)
                CONSTRUCTOR -> Constructor(element.enclosingElement as TypeElement)
                else        -> fail("unexpected element $element")
            }
        }
        is TypeElement       -> {
            val companion = element.getEnclosedClass("Companion")
            if (companion != null && companion.isSubclassOf("hextant.core.editor.TokenType")) {
                val func = companion.getMethod("compile") ?: fail("could not find method 'compile' on $companion")
                CompanionObject(element, func)
            } else {
                val constructor = element
                    .enclosedElements<ExecutableElement>(CONSTRUCTOR)
                    .find { it.parameters.size == 1 && it.parameters[0].asType().toString() == "java.lang.String" }
                if (constructor != null) Constructor(element)
                else fail("no valid constructor or companion object found in class $element")
            }
        }
        else                 -> fail("unexpected element $element")
    }

    private fun Input.isResultNullable(): Boolean = when (this) {
        is Function        -> func.hasAnnotation<Nullable>()
        is Constructor     -> false
        is CompanionObject -> func.hasAnnotation<Nullable>()
    }

    private fun Input.getResultType(): TypeElement = when (this) {
        is Function        -> func.returnType.asTypeElement()
        is Constructor     -> clazz
        is CompanionObject -> clazz
    }

    private fun Input.getFunctionName(): String = when (this) {
        is Function        -> func.simpleName.toString()
        is Constructor     -> clazz.simpleName.toString()
        is CompanionObject -> "${clazz.simpleName}.compile"
    }

    private fun Input.getImports(): List<String> = when (this) {
        is Function        -> listOf(processingEnv.fqName(func), func.returnType.asTypeElement().toString())
        is Constructor     -> listOf(clazz.toString())
        is CompanionObject -> listOf(clazz.toString())
    }

    override fun process(element: Element, annotation: Token) {
        val qn = extractQualifiedEditorClassName(annotation, element)
        val input = getInput(element)
        val resultType = input.getResultType()
        val resultNullable = input.isResultNullable()
        val imports = input.getImports()
        val functionName = input.getFunctionName()
        val (pkg, simpleName) = splitPackageAndSimpleName(qn)
        val result = resultType.simpleName.toString()
        val file = kotlinClass(
            pkg,
            {
                import("hextant.context.Context")
                import("hextant.core.editor.*")
                import("hextant.core.view.*")
                for (fqName in imports) import(fqName)
            },
            simpleName,
            primaryConstructor = { "context" of "Context"; "text" of type("String") },
            inheritance = {
                extend(
                    "TokenEditor".t.parameterizedBy {
                        if (resultNullable) invariant(type(result).nullable())
                        else invariant(result)
                        invariant("TokenEditorView")
                    },
                    "context".e,
                    "text".e
                )
                implementEditorOfSuperType(annotation, result)
            }
        ) {
            addConstructor({ "context" of "Context" }, "context".e, stringLiteral(""))
            addConstructor({
                "context" of "Context"
                "value" of result
            }, "context".e, "value".e call "toString")
            addSingleExprFunction(
                "compile",
                { override() },
                parameters = { "token" of "String" }) {
                call(functionName, "token".e)
            }
        }
        writeKotlinFile(file)
        generatedEditor(resultType, "$pkg.$simpleName")
    }

}