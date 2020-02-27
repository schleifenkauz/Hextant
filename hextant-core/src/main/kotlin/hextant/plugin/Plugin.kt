/**
 *@author Nikolaus Knop
 */

package hextant.plugin

class Plugin(val name: String, val author: String) {
    override fun toString(): String = "$name by $author"
}