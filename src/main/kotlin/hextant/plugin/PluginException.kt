/**
 *@author Nikolaus Knop
 */

package hextant.plugin

class PluginException(plugin: Plugin?, msg: String, cause: Throwable? = null) : Exception(
    "Exception in Plugin ${plugin?.name ?: "plugin name unknown"}: $msg", cause
)