package hextant.impl

import java.nio.file.Path
import java.nio.file.Paths

internal object Settings {
    val logger by myLogger()

    val settings = settingsDirectory()

    private fun settingsDirectory(): Path {
        val home = Paths.get(System.getProperty("user.home"))
        val path = home.resolve(".hextant")
        logger.config("Settings root is $path")
        return path
    }

    val plugins = settings.resolve("plugins.txt")!!
}