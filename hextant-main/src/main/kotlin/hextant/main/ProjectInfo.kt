/**
 *@author Nikolaus Knop
 */

package hextant.main

import hextant.plugins.ProjectType
import kotlinx.serialization.Serializable

@Serializable
internal data class ProjectInfo(
    val projectType: ProjectType,
    val enabledPlugins: List<String>,
    val requiredPlugins: List<String>
)