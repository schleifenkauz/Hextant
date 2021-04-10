package hextant.setup

import hextant.install.*
import hextant.install.OperatingSystem.*
import java.io.File
import java.util.prefs.Preferences
import kotlin.system.exitProcess

object Main {
    private val prefs = Preferences.userRoot()

    @JvmStatic
    fun main(vararg args: String) {
        checkJavaVersion()
        when (args.firstOrNull()) {
            null -> {
                updateMainComponents()
                val launcherDir = File(System.getProperty("user.home"), "hextant/launcher")
                createLauncher(launcherDir)
                prefs.put(LAUNCHER_PATH, launcherDir.absolutePath)
            }
            "help" -> printHelp()
            "make-alias" -> makeAlias()
            "update", "install" -> updateOrInstall(*args)
            "launch" -> {
                if (args.size != 1) fail("Usage: launch")
                open(launcherPath())
            }
            "create-launcher" -> {
                if (args.size != 1) fail("Usage: create-launcher")
                createLauncher()
            }
            "set" -> {
                if (args.size != 3) fail("Usage: set <var> <value>")
                val (_, prop, value) = args
                prefs.put(prop, value)
            }
            else -> {
                if (args.size != 1) fail("Usage: <project>, print help for more info")
                open(args[0].verifyFile())
            }
        }
    }

    private fun printHelp() {
        println("Usage: ")
        println("help: display help information about the Hextant installer")
        println("install: installs the Hextant Core Plugin and the Launcher")
        println("update: updates the Hextant Core Plugin and the Launcher")
        println("install <plugins>: installs the specified plugins")
        println("update <plugins>: updates the specified plugins")
        println("Plugins can either be specified as an URL pointing to their version control repository or as a maven coordinate without version")
        println("launch: launches Hextant")
        println("create-launcher: create a new launcher project")
        println("set <var> <value>: sets the value of a variable in the Hextant preferences")
        println("<project>: Opens a project")
    }

    private fun makeAlias() = CLI {
        val jar = File(javaClass.protectionDomain.codeSource.location.toURI())
        when (OperatingSystem.get()) {
            Windows -> run("doskey", "hextant=java -jar $jar")
            Linux, Mac -> run("alias", "hextant='java -jar $jar'")
        }
    }

    private fun launcherPath(): File = CLI {
        if (prefs.get(LAUNCHER_PATH, null) == null) {
            println("There is no launcher configured.")
            println("Do you want to create one or provide a path to an existing one?")
            println("(1) Create a new launcher, (2) Provide a path to an existing launcher, (3) Quit")
            val path = when (prompt("")) {
                "1" -> createLauncher()
                "2" -> prompt("Location of the launcher")?.verifyFile()
                else -> null
            }
            if (path != null) prefs.put(LAUNCHER_PATH, path.absolutePath)
        }
        prefs.get(LAUNCHER_PATH, null).verifyFile()
    }

    private fun createLauncher(): File = CLI {
        val dest = prompt("Destination for the launcher")?.verifyFile() ?: exitProcess(1)
        createLauncher(dest)
    }

    private fun createLauncher(dest: File): File {
        if (dest.exists()) fail("Launcher destination already exists")
        dest.mkdirs()
        val info = javaClass.getResourceAsStream("launcher-info.json").buffered()
        val out = dest.resolve("project.json").outputStream().buffered()
        info.copyTo(out)
        return dest
    }

    private fun updateOrInstall(vararg args: String) {
        if (args.size == 1) {
            updateMainComponents()
        }
        for (ref in args.drop(1)) {
            updateOrInstall(ref)
        }
    }

    private fun updateMainComponents() {
        updateOrInstall("core")
        updateOrInstall("main")
        updateOrInstall("launcher")
    }

    private fun checkJavaVersion() {
        val version = System.getProperty("java.version")
        if (version < "11") {
            System.err.println("Invalid java version: $version")
            System.err.println("Only version 11 and later is supported")
            exitProcess(1)
        }
    }

    private fun updateOrInstall(ref: String) {
        when (ref) {
            "core" -> HEXTANT_CORE.installOrUpdate(HextantDirectory.resolve("plugins", "core.jar"))
            "main" -> HEXTANT_MAIN.installOrUpdate(HextantDirectory.resolve("plugins", "main.jar"))
            "launcher" -> HEXTANT_LAUNCHER.installOrUpdate(HextantDirectory.resolve("plugins", "launcher.jar"))
            else       -> when {
                ref.startsWith("http")       -> Plugins.installOrUpdatePluginFromSource(ref)
                ref.count { it == ':' } == 1 -> {
                    val (group, artefact) = ref.split(':')
                    Plugins.installOrUpdateFromMaven(group, artefact)
                }
                else                         -> System.err.println("Invalid plugin reference: $ref")
            }
        }
    }

    private fun open(project: File) = CLI {
        val sdk = prefs.get(JAVAFX_SDK, null) ?: System.getenv(JAVAFX_SDK) ?: askJavaFXSDK()
        val core = HextantDirectory.resolve("plugins", "core.jar")
        val main = HextantDirectory.resolve("plugins", "main.jar")
        if (!core.exists() || !main.exists()) {
            System.err.println("Hextant is not yet installed")
            System.err.println("Use 'hextant-setup' to install it")
            exitProcess(1)
        }
        java(
            "--module-path", File(sdk).resolve("lib").absolutePath,
            "--add-modules", "javafx.controls",
            "--add-opens", "java.base/jdk.internal.loader=ALL-UNNAMED",
            "-classpath", "$core$sep$main$sep",
            "hextant.main.Main",
            project.canonicalPath
        )
    }

    private fun CLI.askJavaFXSDK(): String {
        while (true) {
            val path = prompt("Where is your JavaFX SDK located?") ?: continue
            if (testJavaFXSDK(path)) {
                val absolute = File(path).absolutePath
                prefs.put(JAVAFX_SDK, absolute)
                return absolute
            } else {
                System.err.println("JavaFX SDK not recognized")
            }
        }
    }

    private fun testJavaFXSDK(path: String): Boolean {
        val f = File(path).resolve("lib/javafx.base.jar")
        return f.exists()
    }

    private const val GROUP = "com.github.nkb03"
    private val HEXTANT_CORE = MavenCoordinate(GROUP, "hextant-core-fatjar", "1.0-SNAPSHOT")
    private val HEXTANT_MAIN = MavenCoordinate(GROUP, "hextant-main", "1.0-SNAPSHOT")
    private val HEXTANT_LAUNCHER = MavenCoordinate(GROUP, "hextant-launcher", "1.0-SNAPSHOT")
    private const val JAVAFX_SDK = "javafx-sdk"
    private const val LAUNCHER_PATH = "launcher-path"
}