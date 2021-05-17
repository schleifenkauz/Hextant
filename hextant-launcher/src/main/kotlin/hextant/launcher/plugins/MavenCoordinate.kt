/**
 *@author Nikolaus Knop
 */

package hextant.launcher.plugins

import hextant.cli.FileDownload
import java.io.File
import java.net.URL

data class MavenCoordinate(val group: String, val artifact: String, val version: String) {
    operator fun get(type: String): URL = URL("$GET_SNAPSHOT&g=$group&a=$artifact&v=$version&e=$type")

    fun installOrUpdate(dest: File) {
        val oldChecksum = dest.resolveSibling(dest.name + ".sha1")
        val newChecksum = get("jar.sha1").readText()
        if (!dest.exists() || !oldChecksum.exists() || oldChecksum.readText() != newChecksum) {
            oldChecksum.writeText(newChecksum)
            FileDownload.download(get("jar"), dest)
            println("Successfully installed new version of $artifact")
        } else {
            println("$artifact up-to-date")
        }
    }

    companion object {
        val REGEX = Regex("[a-zA-Z][a-zA-Z.\\-0-9]*")

        private const val GET_SNAPSHOT = "https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots"
    }
}