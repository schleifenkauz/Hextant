/**
 *@author Nikolaus Knop
 */

package hextant.serial

import bundles.SimpleProperty
import hextant.Context
import hextant.Editor
import java.nio.file.Path

/**
 * A [FileManager] organizes many [VirtualFile]s, so that for each physical file, there is always only one [VirtualFile]
 */
interface FileManager {
    /**
     * Get the [VirtualFile] associated with the given [path]. This method is caching, which means that
     * if there is already a [VirtualFile] associated with the [path] that one is returned. Otherwise a new
     * [VirtualFile] is created and initialized with the given [content].
     */
    fun <E : Editor<*>> get(content: E, path: Path): VirtualFile<E>

    /**
     * Create a new uninitialized [VirtualFile] with the given [path].
     * @throws IllegalStateException if there is already a [VirtualFile] associated with the given [path].
     */
    fun <E : Editor<*>> from(path: Path, context: Context): VirtualFile<E>

    /**
     * Rename the file or directory with the [old] name to the [new] name.
     */
    fun rename(old: Path, new: Path)

    /**
     * Create a new directory with the given [path]
     */
    fun createDirectory(path: Path)

    /**
     * Delete the physical file represented by [path] and remove the associated [VirtualFile]
     */
    fun deleteFile(path: Path)

    /**
     * Delete the directory associated with the given [path]
     */
    fun deleteDirectory(path: Path)

    companion object : SimpleProperty<FileManager>("file manager") {
        fun newInstance(context: Context): FileManager = PhysicalFileManager(context)
    }
}