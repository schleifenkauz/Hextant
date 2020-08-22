/**
 *@author Nikolaus Knop
 */

package hextant.codegen

import com.google.auto.service.AutoService
import hextant.codegen.aspects.*
import hextant.codegen.editor.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind.ERROR

@AutoService(Processor::class)
internal class MainProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        fqName<Token>(),
        fqName<Compound>(),
        fqName<Alternative>(),
        fqName<Expandable>(),
        fqName<EditableList>(),
        fqName<RequestAspect>(),
        fqName<ProvideFeature>(),
        fqName<ProvideImplementation>(),
        fqName<ProvideProjectType>()
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedOptions(): MutableSet<String> = mutableSetOf(GENERATED_DIR, ACCESSOR_PACKAGE)

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        try {
            for (proc in processors) proc.process(processingEnv, roundEnv)
            EditorClassGen.reprocessGenerated()
            for (proc in processors) proc.finish()
        } catch (e: ProcessingException) {
            processingEnv.messager.printMessage(ERROR, e.message)
        } catch (e: Throwable) {
            processingEnv.messager.printMessage(ERROR, "Unexpected error ${e.message}")
            e.printStackTrace()
        }
        return true
    }

    companion object {
        const val GENERATED_DIR = "kapt.kotlin.generated"
        const val ACCESSOR_PACKAGE = "hextant.aspect.accessor.pkg"

        val processors = listOf(
            TokenEditorCodegen, CompoundEditorCodegen, AlternativeInterfaceCodegen, ExpanderClassGen, ListEditorCodegen,
            AspectCollector, FeatureCollector, ImplementationCollector, ProjectTypeCollector
        )
    }
}