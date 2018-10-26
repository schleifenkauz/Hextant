/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import javafx.scene.layout.Region
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorViewFactory

/**
 * @return a [Region] showing the vew for the given [CommandLine]
 * @param viewFactory the [EditorViewFactory] used to create the views for the parameter editors
 * defaults to the [EditorViewFactory] of the [HextantPlatform]
*/
fun CommandLine.fxView(viewFactory: EditorViewFactory = HextantPlatform[Public, EditorViewFactory]): Region =
        FXCommandLineView(this, viewFactory)