/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder

import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.fx.FXEditorView
import org.nikok.hextant.core.view.builder.fx.FXVerticalEditorViewBuilder

fun fxEditorView(
    platform: HextantPlatform,
    editable: Editable<*>,
    styleCls: String,
    block: VerticalEditorViewBuilder.() -> Unit
): FXEditorView {
    val builder = FXVerticalEditorViewBuilder(platform, editable)
    builder.block()
    val view = builder.build()
    view.node.styleClass.add(styleCls)
    return view
}