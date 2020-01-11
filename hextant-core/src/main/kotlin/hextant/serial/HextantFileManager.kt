/**
 *@author Nikolaus Knop
 */

package hextant.serial

import hextant.Editor
import hextant.bundle.CorePermissions.Public
import hextant.bundle.Property
import java.nio.file.Path

/**
 * A [HextantFileManager] organizes many [HextantFile]s, so that for each physical file, there is always only one [HextantFile]
 */
interface HextantFileManager {
    fun <T : Any> get(content: T, path: ReactivePath): HextantFile<T>

    fun <E : Editor<*>> get(editor: E): HextantFile<E>

    fun <T : Any> from(path: ReactivePath): HextantFile<T>

    fun rename(old: Path, new: Path)

    fun createDirectory(path: Path)

    fun delete(path: ReactivePath)

    companion object : Property<HextantFileManager, Public, Public>("file manager")
}