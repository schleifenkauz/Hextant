package hextant.install

import java.io.File

object HextantDirectory {
    val home = findHome()

    init {
        home.mkdirs()
        resolve("plugins").mkdir()
        resolve("projects").mkdir()
        resolve("plugin-src").mkdir()
    }

    fun resolve(vararg path: String) = home.resolve(path.joinToString("/"))

    private fun findHome(): File {
        val home = System.getenv("HEXTANT_HOME") ?: System.getProperty("user.home") + "/hextant"
        return File(home)
    }
}