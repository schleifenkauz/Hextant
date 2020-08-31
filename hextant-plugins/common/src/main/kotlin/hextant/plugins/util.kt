/**
 * @author Nikolaus Knop
 */

package hextant.plugins

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.jar.JarFile

inline fun <reified T> JarFile.getInfo(name: String): T? {
    val entry = getEntry(name) ?: return null
    val text = getInputStream(entry).bufferedReader().readText()
    return Json.decodeFromString(text)
}