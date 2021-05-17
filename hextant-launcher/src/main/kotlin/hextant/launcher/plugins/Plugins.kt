/**
 *@author Nikolaus Knop
 */

package hextant.launcher.plugins

import hextant.cli.CLI
import hextant.cli.HextantDirectory

object Plugins {
    fun installOrUpdatePluginFromSource(url: String) = CLI(HextantDirectory.get("plugin-src")) {
        val name = url.removeSuffix(".git").substringAfterLast('/')
        val action = if (cd(name)) {
            git("pull", "origin", "master").join()
            "updated"
        } else {
            git("clone", url).join()
            cd(name)
            "installed"
        }
        gradle("build", "-PcorrectErrorTypes=false").join()
        gradle("hextantPublish").join()
        println("Successfully $action plugin $name")
    }

    fun installOrUpdateFromMaven(group: String, artifact: String) {
        if (!group.matches(MavenCoordinate.REGEX) || !artifact.matches(MavenCoordinate.REGEX)) {
            System.err.println("Invalid maven coordinate: $group:$artifact")
            return
        }
        val coord = MavenCoordinate(group, artifact, STABLE_VERSION)
        val dest = HextantDirectory.resolve("plugins", "$artifact.jar")
        coord.installOrUpdate(dest)
    }

    private const val STABLE_VERSION = "STABLE-SNAPSHOT"
}