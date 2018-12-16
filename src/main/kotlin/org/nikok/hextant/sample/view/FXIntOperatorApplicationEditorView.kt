/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.sample.view

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.view.builder.fxEditorView
import org.nikok.hextant.sample.editable.EditableIntOperatorApplication

private fun viewFactory(platform: HextantPlatform) =
    fxEditorView<EditableIntOperatorApplication>(platform, "int-operator-application") { e ->
        view(e.left)
        view(e.op)
        view(e.right)
    }

fun fxIntOperatorApplicationEditorView(
    editable: EditableIntOperatorApplication,
    platform: HextantPlatform
): FXEditorView = viewFactory(platform)(editable)