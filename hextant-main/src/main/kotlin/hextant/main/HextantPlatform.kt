package hextant.main

import bundles.SimpleProperty
import hextant.context.*
import hextant.plugins.Marketplace
import hextant.plugins.client.HttpPluginClient
import javafx.stage.Stage
import java.net.URLClassLoader

object HextantPlatform {
    internal val marketplace = SimpleProperty<Marketplace>("marketplace")

    internal val stage = SimpleProperty<Stage>("stage")

    internal val launcher = SimpleProperty<HextantLauncher>("launcher")

    /**
     * Create the global context, that is, the root of all contexts.
     */
    fun globalContext(): Context = Context.newInstance {
        val gd = GlobalDirectory.inUserHome()
        set(GlobalDirectory, gd)
        set(marketplace, HttpPluginClient("http://localhost:80", gd[GlobalDirectory.PLUGIN_CACHE]))
    }

    /**
     * Create the root context for a project.
     */
    fun projectContext(global: Context = globalContext()) = global.extend {
        val cl = Thread.currentThread().contextClassLoader
        val hcl = if (cl is HextantClassLoader) cl else HextantClassLoader(this, emptyList(), cl as URLClassLoader)
        set(HextantClassLoader, hcl)
        Properties.initializeProjectContext(this, hcl)
    }
}