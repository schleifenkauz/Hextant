package hextant.impl

import java.nio.file.Path
import java.nio.file.Paths

internal object Settings {
    val settings = settingsDirectory()

    private fun settingsDirectory(): Path {
        val home = Paths.get(System.getProperty("user.home"))
        return home.resolve(".hextant")
    }

    val plugins = settings.resolve("plugins.txt")
}