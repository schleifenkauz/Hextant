/**
 *@author Nikolaus Knop
 */

package hextant.project

/**
 * A project item can be either be a [Directory] or a [File].
 */
sealed class ProjectItem<out T> {
    /**
     * The name of this project item.
     */
    abstract val name: String
}

/**
 * Represents a directory.
 * @param items the [ProjectItem]s that are located inside this directory.
 */
data class Directory<out T>(override val name: String, val items: List<ProjectItem<T>>) : ProjectItem<T>()

/**
 * Represents a file.
 * @param content the content of this file.
 */
data class File<out T>(override val name: String, val content: T) : ProjectItem<T>()