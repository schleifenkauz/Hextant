/**
 * @author Nikolaus Knop
 */

package hextant.main

import javafx.application.Application
import java.io.*
import java.net.URL
import java.nio.channels.Channels
import kotlin.system.exitProcess

internal object Main {
    @JvmStatic fun main(args: Array<String>) {
        System.err.println(args.joinToString(" "))
        addCoreToClassPath()
        try {
            Application.launch(HextantApp::class.java, *args)
        } catch (io: IOException) {
            io.printStackTrace()
            fail("Unexpected IO error: ${io.message}")
        } catch (ex: Exception) {
            ex.printStackTrace()
            fail("Unexpected exception: ${ex.message}")
        } catch (err: Error) {
            err.printStackTrace()
            fail("Unexpected error: ${err.message}")
        }
    }

    internal fun fail(message: String): Nothing {
        System.err.println(message)
        exitProcess(1)
    }

    private fun addCoreToClassPath() {
        try {
            val plugins = File(System.getProperty("user.home"), "hextant/plugins")
            plugins.mkdirs()
            val core = plugins.resolve("core.jar")
            if (!core.exists()) downloadHextantCore(core)
            addURLToSystemClassLoader(core.toURI().toURL())
        } catch (ex: Throwable) {
            ex.printStackTrace()
            fail("Error while tweaking classpath")
        }
    }

    private fun downloadHextantCore(dest: File) {
        val url = URL(CORE_SNAPSHOT)
        val rbc = Channels.newChannel(url.openStream())
        val fos = FileOutputStream(dest)
        fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
    }

    private const val MAVEN_CENTRAL = "https://repo1.maven.org/maven2"
    private const val CORE_DEP = "com/github/nkb03/hextant-core"
    private const val CORE_VERSION = "0.1"
    private const val GET_SNAPSHOT = "https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots"
    private const val SNAPSHOT_VERSION = "1.0-SNAPSHOT"
    private const val CORE_RELEASE = "$MAVEN_CENTRAL/$CORE_DEP/$CORE_VERSION/hextant-core-$CORE_VERSION.jar"
    private const val CORE_SNAPSHOT = "$GET_SNAPSHOT&g=com.github.nkb03&a=hextant-core&v=$SNAPSHOT_VERSION&e=jar"
}