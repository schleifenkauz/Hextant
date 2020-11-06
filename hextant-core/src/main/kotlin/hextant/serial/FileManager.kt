/**
 *@author Nikolaus Knop
 */

package hextant.serial

import bundles.SimpleProperty
import hextant.context.Context
import hextant.core.Editor
import java.io.File

/**
 * A [FileManager] organizes many [VirtualFile]s, so that for each physical file, there is always only one [VirtualFile]
 */
interface FileManager {
    /**
     * Get the [VirtualFile] associated with the given [path]. This method is caching, which means that
     * if there is already a [VirtualFile] associated with the [path] that one is returned. Otherwise a new
     * [VirtualFile] is created and initialized with the given [content].
     */
    fun <E : Editor<*>> get(content: E, path: File): VirtualFile<E>

    /**
     * Create a new uninitialized [VirtualFile] with the given [path].
     * @throws IllegalStateException if there is already a [VirtualFile] associated with the given [path].
     */
    fun <E : Editor<*>> from(path: File, context: Context): VirtualFile<E>

    /**
     * Rename the file or directory with the [old] name to the [new] name.
     */
    fun rename(old: File, new: File)

    /**
     * Create a new directory with the given [path]
     */
    fun createDirectory(path: File)

    /**
     * Delete the physical file represented by [path] and remove the associated [VirtualFile]
     */
    fun deleteFile(path: File)

    /**
     * Delete the directory associated with the given [dir]
     */
    fun deleteDirectory(dir: File)

    companion object : SimpleProperty<FileManager>("file manager")
}