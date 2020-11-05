package hextant.setup

import hextant.install.*
import java.io.File
import java.util.prefs.Preferences
import kotlin.system.exitProcess

object Main {
    private val prefs = Preferences.userNodeForPackage(javaClass)

    @JvmStatic
    fun main(vararg args: String) {
        checkJavaVersion()
        if (args.isEmpty()) {
            launch()
            return
        }
        when (args[0]) {
            "update", "install" -> updateOrInstall(*args)
            "launch" -> launch()
            "set" -> {
                if (args.size != 3) {
                    System.err.println("The 'set'-command expects exactly 2 arguments")
                    exitProcess(1)
                }
                val (_, prop, value) = args
                prefs.put(prop, value)
            }
        }
    }

    private fun updateOrInstall(vararg args: String) {
        if (args.size == 1) {
            installOrUpdate("core")
            installOrUpdate("launcher")
        }
        for (ref in args.drop(1)) {
            installOrUpdate(ref)
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

    private fun installOrUpdate(ref: String) {
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