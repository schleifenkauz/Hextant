package hextant.test

import bundles.set
import hextant.context.Context
import hextant.context.Internal
import hextant.context.Properties.logger
import hextant.inspect.Inspections
import hextant.plugins.Aspects
import hextant.undo.UndoManager
import java.util.logging.Logger
import kotlin.reflect.full.companionObjectInstance

fun testingContext() = Context.create {
    set(UndoManager, UndoManager.newInstance())
    val internal = Internal::class.companionObjectInstance as Internal
    set(internal, logger, Logger.getLogger("Hextant Test Logger"))
    set(internal, Aspects, Aspects())
    set(internal, Inspections, Inspections.newInstance())
}