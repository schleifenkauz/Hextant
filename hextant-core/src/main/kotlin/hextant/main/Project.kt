/**
 *@author Nikolaus Knop
 */

package hextant.main

import kotlinx.serialization.Serializable

@Serializable
internal data class Project(val plugins: List<String>)