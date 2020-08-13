/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import krobot.api.KotlinFile
import krobot.api.writeTo
import java.nio.file.Files
import java.nio.file.Paths
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic.Kind.ERROR

internal inline fun <E, reified F> List<E>.mapToArray(f: (E) -> F) = Array(size) { idx -> f(get(idx)) }

internal fun writeToFile(generatedDir: String, pkg: String?, simpleName: String, file: KotlinFile) {
    val packages = pkg?.split('.')?.toTypedArray() ?: emptyArray()
    val path = Paths.get(generatedDir, *packages, "$simpleName.kt")
    Files.createDirectories(path.parent)
    file.writeTo(path)
}

internal fun fail(msg: String): Nothing {
    throw ProcessingException(msg)
}

internal inline fun ensure(condition: Boolean, message: () -> String) {
    if (!condition) {
        val msg = message()
        fail(msg)
    }
}

internal inline fun ProcessingEnvironment.executeSafely(action: () -> Unit) {
    try {
        action()
    } catch (e: ProcessingException) {
        messager.printMessage(ERROR, e.message)
    } catch (e: Throwable) {
        messager.printMessage(ERROR, "Unexpected error ${e.message}")
        e.printStackTrace()
    }
}