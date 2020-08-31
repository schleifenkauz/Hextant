/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.jar.JarFile
import kotlin.properties.ReadOnlyProperty

internal fun <T : Any> background(
    scope: CoroutineScope = GlobalScope,
    compute: suspend () -> T
): ReadOnlyProperty<Any?, T> {
    var cached: T? = null
    val job = scope.launch { cached = compute() }
    return ReadOnlyProperty { _, _ ->
        if (cached != null) cached!!
        else runBlocking {
            job.join()
            cached!!
        }
    }
}

inline fun <reified T> JarFile.getInfo(name: String): T? {
    val entry = getEntry(name) ?: return null
    val text = getInputStream(entry).bufferedReader().readText()
    return Json.decodeFromString(text)
}