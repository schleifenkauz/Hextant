/**
 *@author Nikolaus Knop
 */

package hextant.main

import bundles.*
import hextant.command.Commands
import hextant.command.line.*
import hextant.context.*
import hextant.core.Editor
import hextant.core.InputMethod
import hextant.core.editor.getSimpleEditorConstructor
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.plugin.Aspects
import hextant.plugins.Marketplace
import hextant.plugins.client.HttpPluginClient
import hextant.serial.SerialProperties
import hextant.serial.SerialProperties.deserializationContext
import hextant.settings.model.ConfigurableProperties
import hextant.undo.UndoManager
import javafx.stage.Stage
import kserial.KSerial
import kserial.SerialContext
import java.net.URLClassLoader
import java.util.logging.Logger

/**
 * The hextant platform is responsible for creating the root context.
 */
object HextantPlatform {
    /**
     * The logger property
     */
    val logger = Property<Logger, Any, Internal>("top level logger")


    /**
     * The [PropertyChangeHandlers] is used to register properties for views.
     */
    val propertyChangeHandlers = Property<PropertyChangeHandlers, Any, Internal>("property change handlers")

    internal val marketplace = SimpleProperty<Marketplace>("marketplace")

    internal val globalContext = globalContext()

    internal val launcherContext = defaultContext(projectContext(globalContext))

    internal val stage = SimpleProperty<Stage>("stage")

    internal val launcher = SimpleProperty<HextantLauncher>("launcher")

    /**
     * Create the global context, that is, the root of all contexts.
     */
    internal fun globalContext(): Context = Context.newInstance {
        val gd = GlobalDirectory.inUserHome()
        set(GlobalDirectory, gd)
        set(marketplace, HttpPluginClient("http://localhost:80", gd[GlobalDirectory.PLUGIN_CACHE]))
        set(ProjectManager, ProjectManager(this))
    }

    /**
     * Create the root context for a project.
     */
    fun projectContext(global: Context = globalContext()) =
        global.extend {
            val cl = Thread.currentThread().contextClassLoader
            val hcl = if (cl is HextantClassLoader) cl else HextantClassLoader(this, emptyList(), cl as URLClassLoader)
            set(HextantClassLoader, hcl)
            set(Internal, Commands, Commands.newInstance())
            set(Internal, Inspections, Inspections.newInstance())
            set(Internal, Stylesheets, Stylesheets(hcl))
            set(Internal, logger, Logger.getLogger(javaClass.name))
            set(Internal, propertyChangeHandlers, PropertyChangeHandlers())
            set(Internal, SerialProperties.serialContext, createSerialContext())
            set(Internal, SerialProperties.serial, KSerial.newInstance())
            set(Internal, ConfigurableProperties, ConfigurableProperties())
            set(Internal, Aspects, Aspects(cl))
        }

    /**
     * Create the serial context.
     */
    private fun createSerialContext(): SerialContext = SerialContext.newInstance {
        useUnsafe = true
        registerConstructor<Editor<*>> { bundle, cls ->
            cls.getSimpleEditorConstructor().invoke(bundle[deserializationContext])
        }
    }

    /**
     * Extend the [projectContext] with some core properties.
     */
    fun defaultContext(root: Context) = root.extend {
        set(SelectionDistributor, SelectionDistributor.newInstance())
        set(EditorControlGroup, EditorControlGroup())
        set(UndoManager, UndoManager.newInstance())
        set(Clipboard, SimpleClipboard())
        set(InputMethod, InputMethod.REGULAR)
        val clContext = extend {
            set(SelectionDistributor, SelectionDistributor.newInstance())
        }
        set(CommandLine, CommandLine(clContext, ContextCommandSource(this, *CommandReceiverType.values())))
    }
}