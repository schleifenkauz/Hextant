package hextant.install

import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

fun fail(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}

fun String.verifyFile(): File = try {
    val f = File(this)
    f.canonicalPath
    f
} catch (ex: IOException) {
    fail("Invalid or non-existent file path: $this")
}