/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder.gui.editor

import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.editable.Expandable
import org.nikok.hextant.core.editor.ConfiguredExpander
import org.nikok.hextant.core.editor.ExpanderConfig
import org.nikok.hextant.core.view.builder.gui.editable.EditableEditorViewPart

class EditorViewPartExpander(
    editable: Expandable<*, EditableEditorViewPart>,
    platform: HextantPlatform
) : ConfiguredExpander<EditableEditorViewPart>(config, editable, platform) {
    companion object {
        val config = ExpanderConfig<EditableEditorViewPart>().apply {
            registerConstant("space") { TODO() }
            registerConstant("keyword") { TODO() }
            registerConstant("operator") { TODO() }
        }
    }
}