/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder.fx

import javafx.scene.layout.HBox
import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.fx.HextantTextField
import org.nikok.hextant.core.fx.initSelection
import org.nikok.hextant.core.view.builder.HorizontalEditorViewBuilder

class FXHorizontalEditorViewBuilder(
    platform: HextantPlatform,
    editable: Editable<*>
) : FXEditorViewBuilder(platform, editable, HBox()), HorizontalEditorViewBuilder {
    override fun spaces(n: Int) {
        val text = " ".repeat(n - 1)
        val tf = HextantTextField(text)
        tf.initSelection(editor)
        tf.isEditable = false
        addChild(tf)
    }

    override fun space() = spaces(1)
}