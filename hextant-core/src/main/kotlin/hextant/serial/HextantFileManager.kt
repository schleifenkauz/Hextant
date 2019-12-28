/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import java.nio.file.Path

/**
 * A [HextantFileManager] organizes many [HextantFile]s, so that for each physical file, there is always only one [HextantFile]
 */
interface HextantFileManager {
    /**
     * Return the [HextantFile] associated with the given [path]
     * @throws NoSuchElementException if no [HextantFile] with the given [path] has been created by this [HextantFileManager]
     */
    fun <T : Any> get(path: Path): HextantFile<T>

    /**
     * Create a new [HextantFile] with the given [content] and [path] return it
     * @throws IllegalStateException if this [HextantFileManager] already created a file with the given [path]
     */
    fun <T : Any> create(path: Path, content: T): HextantFile<T>

    /**
     * Delete the [HextantFile] associated with the given [path] both physically and virtually.
     * @throw IllegalStateException if there is no [HextantFile] associated with the given [path]
     */
    fun delete(path: Path)

    companion object : Property<HextantFileManager, Public, Public>("file manager")
}