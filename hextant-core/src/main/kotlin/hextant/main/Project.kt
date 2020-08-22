/**
 *@author Nikolaus Knop
 */

package hextant.main

import kotlinx.serialization.Serializable

@Serializable
data class Project(val plugins: List<String>)