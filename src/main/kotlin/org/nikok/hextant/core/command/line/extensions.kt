/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.command.line

import javafx.scene.layout.Region
import org.nikok.hextant.HextantPlatform

/**
 * @return a [Region] showing the vew for the given [CommandLine]
 */
fun CommandLine.fxView(platform: HextantPlatform): Region =
        FXCommandLineView(this, platform)