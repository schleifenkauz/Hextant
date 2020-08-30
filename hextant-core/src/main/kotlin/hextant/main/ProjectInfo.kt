/**
 *@author Nikolaus Knop
 */

package hextant.main

import kotlinx.serialization.Serializable

@Serializable
internal data class ProjectInfo(val enabledPlugins: List<String>, val requiredPlugins: List<String>)