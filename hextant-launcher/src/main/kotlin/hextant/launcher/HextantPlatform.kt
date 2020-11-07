package hextant.launcher

import bundles.publicProperty
import bundles.set
import hextant.context.Context
import hextant.context.Properties
import hextant.plugins.LocalPluginRepository
import hextant.plugins.Marketplace
import javafx.stage.Stage

object HextantPlatform {
    internal val marketplace = publicProperty<Marketplace>("marketplace")

    internal val stage = publicProperty<Stage>("stage")

    internal val launcher = publicProperty<HextantLauncher>("launcher")

    /**
     * Create the global context, that is, the root of all contexts.
     */
    fun globalContext(): Context = Context.newInstance {
        val gd = Files.inUserHome()
        set(Files, gd)
        set(marketplace, LocalPluginRepository(gd[Files.PLUGIN_CACHE]))
    }

    /**
     * Create the root context for a project.
     */
    fun projectContext(global: Context): Context {
        val cl = Thread.currentThread().contextClassLoader
        val hcl = if (cl is HextantClassLoader) cl else HextantClassLoader(global, emptyList(), cl)
        return Properties.projectContext(global, hcl)
    }
}