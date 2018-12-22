package hextant.impl

import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger

internal object Settings {
    val settings = settingsDirectory()

    private fun settingsDirectory(): Path {
        val home = Paths.get(System.getProperty("user.home"))
        val path = home.resolve(".hextant")
        logger.config("Settings root is $path")
        return path
    }

    val plugins = settings.resolve("plugins.txt")!!

    val logger = Logger.getLogger("settings")
}