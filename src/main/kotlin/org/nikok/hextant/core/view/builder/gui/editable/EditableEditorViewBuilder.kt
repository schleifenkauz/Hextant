/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder.gui.editable

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.base.UnitEditable
import org.nikok.hextant.core.expr.editable.EditableText
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.list.EditableList
import org.nikok.hextant.core.view.builder.fxEditorView
import org.nikok.hextant.core.view.builder.gui.EditorViewPart
import org.nikok.reaktive.collection.binding.count
import org.nikok.reaktive.value.ReactiveBoolean
import org.nikok.reaktive.value.now

class EditableEditorViewBuilder<E : Editable<*>>
    : UnitEditable() {
    val lines = EditableList<List<EditorViewPart?>, EditableEditorViewLine>()

    val styleCls = EditableText(parent = this)

    override val isOk: ReactiveBoolean =
        lines.editableList.count("helper for all") { it.isOk.now }.map("is ok") { it == 0 }

    override val children: Collection<Editable<*>>?
        get() = lines.children!! + styleCls

    fun getEditorViewFactory(platform: HextantPlatform): (E) -> FXEditorView {
        return fxEditorView(platform, styleCls.text.now) {
            lines.editedList.now.forEach { line ->
                line?.forEach { part ->
                    line {
                        part?.invoke(this)
                    }
                }
            }
        }
    }
}