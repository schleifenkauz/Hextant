/**
 *@author Nikolaus Knop
 */

package hextant.project

sealed class ProjectItem<out T> {
    abstract val name: String
}

data class Directory<out T>(override val name: String, val items: List<ProjectItem<T>>) : ProjectItem<T>()

data class File<out T>(override val name: String, val content: T) : ProjectItem<T>()