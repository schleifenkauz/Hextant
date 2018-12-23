package hextant.impl

import java.nio.file.Path
import java.nio.file.Paths

internal object Resources {
    val root: Path = getResourcesRoot()

    val all: Set<Path> by lazy {
        root.toFile()
            .walkBottomUp()
            .filter { it.isFile && !it.name.startsWith("_") }
            .map { file ->
                logger.info { "Found resource $file" }
                file.toPath()
            }
            .toSet()
    }

    fun allWithExtension(ext: String): List<Path> {
        return all.filter { it.toString().endsWith(ext) }
    }

    fun allCSS() = allWithExtension(".css")

    private fun getResourcesRoot(): Path =
        javaClass.classLoader.getResources("").asSequence()
            .find { root ->
                root.toExternalForm().endsWith("resources/")
            }?.let { url -> Paths.get(url.toURI()) }
            ?: throw RuntimeException("Resources root not found")

    val logger by myLogger()
}