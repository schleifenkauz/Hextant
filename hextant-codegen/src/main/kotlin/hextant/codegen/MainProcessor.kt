/**
 *@author Nikolaus Knop
 */

package hextant.codegen

import hextant.codegen.aspects.*
import hextant.codegen.editor.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind.ERROR

internal class MainProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        fqName<Token>(),
        fqName<Compound>(),
        fqName<Alternative>(),
        fqName<EditorInterface>(),
        fqName<Expandable>(),
        fqName<EditableList>(),
        fqName<RequestAspect>(),
        fqName<ProvideFeature>(),
        fqName<ProvideImplementation>(),
        fqName<ProvideProjectType>(),
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedOptions(): MutableSet<String> = mutableSetOf(GENERATED_DIR, ACCESSOR_PACKAGE)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) return false
        try {
            for (proc in processors) proc.init(processingEnv)
            for (proc in editorCodegen) proc.preprocess(roundEnv)
            for (proc in editorCodegen) proc.process(roundEnv)
            for (proc in collectors) proc.process(roundEnv)
            EditorClassGen.reprocessGenerated()
            for (proc in collectors) proc.finish()
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

        private val editorCodegen = listOf(
            TokenEditorCodegen,
            CompoundEditorCodegen,
            AlternativeInterfaceCodegen,
            ExpanderClassGen,
            ListEditorCodegen
        )

        private val collectors = listOf(
            AspectCollector,
            FeatureCollector,
            ImplementationCollector,
            ProjectTypeCollector
        )

        private val processors = editorCodegen + collectors
    }
}