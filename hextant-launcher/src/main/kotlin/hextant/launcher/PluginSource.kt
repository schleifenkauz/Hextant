/**
 * @author Nikolaus Knop
 */

package hextant.launcher

import java.net.URL

sealed class PluginSource {
    data class GitRepo(val url: URL) : PluginSource() {
        override fun toString(): String = url.toExternalForm().removeSuffix(".git").substringAfterLast('/')
    }

    data class MavenCoordinate(val group: String, val artifact: String) : PluginSource() {
        override fun toString(): String = artifact
    }
}