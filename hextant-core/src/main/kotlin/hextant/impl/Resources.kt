package hextant.impl

import java.nio.file.Path
import java.nio.file.Paths

internal object Resources {
    val roots = getResourceRoots()

    val all: Set<Path> by lazy {
        roots.asSequence().flatMap { root ->
            root.toFile()
                .walkBottomUp()
                .filter { it.isFile && !it.name.startsWith("_") }
                .map { file ->
                    logger.info { "Found resource $file" }
                    file.toPath()
                }
        }.toSet()
    }

    fun allWithExtension(ext: String): List<Path> {
        return all.filter { it.toString().endsWith(ext) }
    }

    fun allCSS() = allWithExtension(".css")

    private fun getResourceRoots(): Set<Path> =
        javaClass.classLoader.getResources("").asSequence()
            .filter { root ->
                root.toExternalForm().endsWith("resources/")
            }.map { url -> Paths.get(url.toURI()) }.toSet()

    val logger by myLogger()
}