/**
 * @author Nikolaus Knop
 */

package hextant.command.line

import hextant.Context
import hextant.bundle.Bundle
import javafx.scene.layout.Region

/**
 * @return a [Region] showing the vew for the given [CommandLine]
 */
fun CommandLine.fxView(context: Context): Region =
    FXCommandLineView(this, context, Bundle.newInstance())