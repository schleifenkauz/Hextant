/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.view.builder.fx.FXVerticalEditorViewBuilder

fun <E : Editable<*>> fxEditorView(
    platform: HextantPlatform,
    styleCls: String,
    block: VerticalEditorViewBuilder.(E) -> Unit
): (E) -> FXEditorView = { editable ->
    val builder = FXVerticalEditorViewBuilder(platform, editable)
    builder.block(editable)
    builder.build().apply {
        node.styleClass.add(styleCls)
    }
}