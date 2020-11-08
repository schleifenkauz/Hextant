package hextant.install

enum class OperatingSystem {
    Linux, Windows, Mac;

    companion object {
        private val cached by lazy {
            val name = System.getProperty("os.name").toLowerCase()
            when {
                "windows" in name                 -> Windows
                "nux" in name                     -> Linux
                "mac" in name || "darwin" in name -> Mac
                else                              -> error("Unrecognized operating system $name")
            }
        }

        fun get(): OperatingSystem = cached
    }
}