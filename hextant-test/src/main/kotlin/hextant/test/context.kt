package hextant.test

import bundles.set
import hextant.context.Context
import hextant.context.EditorControlGroup
import hextant.context.Internal
import hextant.context.Properties.logger
import hextant.context.SelectionDistributor
import hextant.fx.Stylesheets
import hextant.inspect.Inspections
import hextant.plugins.Aspects
import hextant.undo.UndoManager
import java.util.logging.Logger

fun testingContext() = Context.create {
    set(UndoManager, UndoManager.newInstance())
    set(Internal, logger, Logger.getLogger("Hextant Test Logger"))
    set(Internal, Aspects, Aspects())
    set(Internal, Inspections, Inspections.newInstance())
    set(SelectionDistributor, SelectionDistributor.newInstance())
    set(Internal, Stylesheets, Stylesheets())
    set(EditorControlGroup, EditorControlGroup())
}