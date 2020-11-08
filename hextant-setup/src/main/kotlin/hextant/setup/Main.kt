package hextant.setup

import hextant.install.*
import hextant.install.OperatingSystem.*
import java.io.File
import java.util.prefs.Preferences
import kotlin.system.exitProcess

object Main {
    private val prefs = Preferences.userNodeForPackage(javaClass)

    @JvmStatic
    fun main(vararg args: String) {
        checkJavaVersion()
        when (args.firstOrNull()) {
            null -> launch()
            "help" -> printHelp()
            "make-alias" -> makeAlias()
            "update", "install" -> updateOrInstall(*args)
            "launch" -> {
                if (args.size != 1) fail("Usage: launch")
                launch()
            }
            "set" -> {
                if (args.size != 3) fail("Usage: set <var> <value>")
                val (_, prop, value) = args
                prefs.put(prop, value)
            }
            else                -> fail("Unrecognized command ${args[0]}. Use 'help' to print help.")
        }
    }

    private fun fail(message: String) {
        System.err.println(message)
        exitProcess(1)
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
        println("set <var> <value>: sets the value of a variable in the Hextant preferences")
    }

    private fun makeAlias() = CLI {
        val jar = File(javaClass.protectionDomain.codeSource.location.toURI())
        when (OperatingSystem.get()) {
            Windows -> run("doskey", "hextant=java -jar $jar")
            Linux, Mac -> run("alias", "hextant='java -jar $jar'")
        }
    }

    private fun updateOrInstall(vararg args: String) {
        if (args.size == 1) {
            updateOrInstall("core")
            updateOrInstall("launcher")
        }
        for (ref in args.drop(1)) {
            updateOrInstall(ref)
        }
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
            "launcher" -> HEXTANT_LAUNCHER.installOrUpdate(HextantDirectory.resolve("launcher.jar"))
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

    private fun launch() = CLI {
        val sdk = prefs.get(JAVAFX_SDK, null) ?: System.getenv(JAVAFX_SDK) ?: askJavaFXSDK()
        val core = HextantDirectory.resolve("plugins", "core.jar")
        val launcher = HextantDirectory.resolve("launcher.jar")
        if (!core.exists() || !launcher.exists()) {
            System.err.println("Hextant is not yet installed")
            System.err.println("Use 'hextant install' to install it")
            exitProcess(1)
        }
        java(
            "--module-path", File(sdk).resolve("lib").absolutePath,
            "--add-modules", "javafx.controls",
            "--add-opens", "java.base/jdk.internal.loader=ALL-UNNAMED",
            "-classpath", "$launcher${File.pathSeparatorChar}$core",
            "hextant.launcher.Main"
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
    private val HEXTANT_LAUNCHER = MavenCoordinate(GROUP, "hextant-launcher", "1.0-SNAPSHOT")
    private const val JAVAFX_SDK = "javafx-sdk"
}