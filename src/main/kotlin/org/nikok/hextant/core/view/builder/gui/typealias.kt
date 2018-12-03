/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.view.builder.gui

import org.nikok.hextant.core.view.builder.HorizontalEditorViewBuilder
import org.nikok.hextant.core.view.builder.VerticalEditorViewBuilder

typealias EditorViewLine = List<EditorViewPart>
typealias EditorViewPart = HorizontalEditorViewBuilder.() -> Unit
typealias EditorViewBuilder = List<VerticalEditorViewBuilder.() -> Unit>