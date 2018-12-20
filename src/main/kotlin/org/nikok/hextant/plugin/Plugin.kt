/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.plugin

class Plugin(val name: String, val author: String) {
    private val properties = mutableMapOf<String, Any>()

    internal fun setProperty(name: String, value: Any) {
        properties[name] = value
    }

    fun getProperty(name: String) = properties[name]
}