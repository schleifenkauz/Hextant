/**
 *@author Nikolaus Knop
 */

package hextant.install

object Plugins {
    fun installOrUpdatePluginFromSource(url: String) {
        CLI(HextantDirectory.resolve("plugin-src")) {
            val name = url.removeSuffix(".git").substringAfterLast('/')
            val action = if (cd(name)) {
                git("pull", "origin", "master")
                "updated"
            } else {
                git("clone", url)
                cd(name)
                "installed"
            }
            gradle("build", "-PcorrectErrorTypes=false")
            gradle("hextantPublish")
            println("Successfully $action plugin $url")
        }
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