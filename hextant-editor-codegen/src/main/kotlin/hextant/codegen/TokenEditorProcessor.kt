/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import com.google.auto.service.AutoService
import krobot.api.*
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class TokenEditorProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Token::class.qualifiedName!!)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(Token::class.java).forEach { e ->
            val name = e.simpleName.toString()
            println("Processing $name")
            val ann = e.getAnnotation(Token::class.java)
            val pkg = ann.pkg.orDefault { processingEnv.elementUtils.getPackageOf(e).toString() + ".editor" }
            val generatedName = ann.name.orDefault { "${e.simpleName}Editor" }
            val file = kotlinClass(
                pkg,
                {
                    import("hextant.core.editor.TokenEditor")
                    import(e.toString())
                    import("hextant.Context")
                    import("hextant.core.TokenType")
                    import("hextant.core.view.TokenEditorView")
                },
                generatedName,
                primaryConstructor = { "context" of type("Context"); "text" of type("String") },
                inheritance = {
                    extend(
                        type("TokenEditor").parameterizedBy {
                            covariant(name)
                            covariant("TokenEditorView")
                        },
                        getVar("context"),
                        getVar("text")
                    )
                    implement(type("TokenType").parameterizedBy { covariant(name) }, delegate = getVar(name))
                }
            )
            val generatedDir = processingEnv.options["kapt.kotlin.generated"]
            val path = Paths.get(generatedDir, *pkg.split('.').toTypedArray(), "$generatedName.kt")
            Files.createDirectories(path.parent)
            file.writeTo(path)
        }
        return true
    }
}