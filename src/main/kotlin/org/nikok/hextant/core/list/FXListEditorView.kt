/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.list

import javafx.scene.Node
import javafx.scene.layout.VBox
import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.prop.CorePermissions.Public

class FXListEditorView(private val viewFactory: EditorViewFactory = HextantPlatform[Public, EditorViewFactory]) :
        ListEditorView,
        FXEditorView,
        VBox() {
    override fun added(editable: Editable<*>, idx: Int) {

    }

    override fun removed(idx: Int) {
        TODO("not implemented")
    }

    override val node: Node
        get() = TODO("not implemented")
}