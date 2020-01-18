/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.Context
import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import hextant.project.editor.FileEditor
import java.nio.file.Path

/**
 * A [HextantFileManager] organizes many [HextantFile]s, so that for each physical file, there is always only one [HextantFile]
 */
interface HextantFileManager {
    /**
     * Get the [HextantFile] associated with the given [path]. This method is caching, which means that
     * if there is already a [HextantFile] associated with the [path] that one is returned. Otherwise a new
     * [HextantFile] is created and initialized with the given [content].
     */
    fun <T : Any> get(content: T, path: ReactivePath): HextantFile<T>

    /**
     * Uses the [FileEditor.path] of the [Editor.file] of the given [editor], to get a [HextantFile]
     * @throws IllegalStateException if the given [editor] is not a [root]-editor.
     */
    fun <E : Editor<*>> get(editor: E): HextantFile<E>

    /**
     * Create a new uninitialized [HextantFile] with the given [path].
     * @throws IllegalStateException if there is already a [HextantFile] associated with the given [path].
     */
    fun <T : Any> from(path: ReactivePath): HextantFile<T>

    /**
     * Rename the file or directory with the [old] name to the [new] name.
     */
    fun rename(old: Path, new: Path)

    /**
     * Create a new directory with the given [path]
     */
    fun createDirectory(path: Path)

    /**
     * Delete the physical file represented by [path] and remove the associated [HextantFile]
     */
    fun deleteFile(path: ReactivePath)

    /**
     * Delete the directory associated with the given [path]
     */
    fun deleteDirectory(path: ReactivePath)

    companion object : Property<HextantFileManager, Public, Public>("file manager") {
        fun newInstance(context: Context): HextantFileManager = HextantFileManagerImpl(context)
    }
}