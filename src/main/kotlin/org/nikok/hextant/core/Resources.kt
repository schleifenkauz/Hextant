package org.nikok.hextant.core

import java.nio.file.Path
import java.nio.file.Paths

internal object Resources {
    fun all(): Set<Path> =
            root.toFile()
                    .walkBottomUp()
                    .filter { it.isFile && !it.name.startsWith("_") }
                    .map { it.toPath() }
                    .toSet()

    fun allWithExtension(ext: String): List<Path> {
        return all().filter { it.toString().endsWith(ext) }
    }

    fun allCSS() = allWithExtension(".css")

    val root: Path = getResourcesRoot()

    private fun getResourcesRoot(): Path {
        val cls = this.javaClass
        val url = cls.getResource("resource")
        val path = Paths.get(url.toURI())
        val nc = path.nameCount
        return path.root.resolve(path.subpath(0, nc - 5))
    }
}