/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.impl

import java.nio.file.*
import java.util.*
import kotlin.reflect.KClass

class ClassFinder private constructor(private val classLoader: ClassLoader) {
    class ClassInfo(val fullName: String, private val classLoader: ClassLoader) {
        val packageName by lazy { fullName.replaceAfterLast('.', "").dropLast(1) }

        val className by lazy { fullName.replaceBeforeLast('.', "").drop(1) }

        val isTopLevel: Boolean get() = '$' !in className

        fun loadClass(): KClass<*> = classLoader.loadClass(fullName).kotlin

        override fun toString(): String = fullName
    }

    val allClasses: Set<ClassInfo> by lazy { findAllClasses() }

    /**
     * @return all classes that are not inner classes or lambdas
     */
    val allTopLevel by lazy {
        allClasses.filter { it.isTopLevel }
    }

    private fun findAllClasses(): Set<ClassInfo> {
        val res = mutableSetOf<ClassInfo>()
        val roots = classLoader.getResources("")
        for (root in roots) {
            logger.finest { "Found resource root $root" }
            val path = Paths.get(root.toURI()) ?: throw AssertionError()
            addAllClassesTo(path, res)
        }
        return res
    }

    private fun addAllClassesTo(
        path: Path,
        set: MutableCollection<ClassInfo>
    ) {
        if (Files.isDirectory(path)) {
            logger.finest { "found directory $path" }
            for (c in Files.list(path)) {
                addAllClassesTo(c, set)
            }
        } else if (Files.isRegularFile(path)) {
            logger.finest { "found file $path" }
            if (path.toString().endsWith(".class")) {
                logger.finest { "has class extensions" }
                val qualifiedName = qualifiedClassName(path)
                logger.finest { "Full class name: $qualifiedName" }
                val info = ClassInfo(qualifiedName, classLoader)
                set.add(info)
            }
        }
    }

    companion object {
        val logger by myLogger()

        private const val CLASS_EXTENSION = ".class"

        private fun qualifiedClassName(path: Path): String {
            return path.dropWhile { it.toString() != "org" } //only until package root
                .joinToString(separator = ".") //replace slashes by dots
                .removeSuffix(CLASS_EXTENSION)
                .removeSuffix("Kt")
        }

        private val cache = IdentityHashMap<ClassLoader, ClassFinder>()

        fun get(classLoader: ClassLoader) = cache.getOrPut(classLoader) {
            ClassFinder(
                classLoader
            )
        }!!
    }
}
