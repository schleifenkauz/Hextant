package org.nikok.hextant.impl

import java.nio.file.Path
import java.nio.file.Paths

internal object IO {
    val settings = settingsDirectory()

    private fun settingsDirectory(): Path {
        val home = Paths.get(System.getProperty("user.home"))
        return home.resolve(".hextant")
    }
}