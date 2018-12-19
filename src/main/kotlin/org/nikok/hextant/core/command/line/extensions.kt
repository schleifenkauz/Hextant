/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import javafx.scene.layout.Region
import org.nikok.hextant.Context

/**
 * @return a [Region] showing the vew for the given [CommandLine]
 */
fun CommandLine.fxView(context: Context): Region =
    FXCommandLineView(this, context)