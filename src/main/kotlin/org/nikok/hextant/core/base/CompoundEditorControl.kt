/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.base

import javafx.scene.control.Label
import javafx.scene.layout.*
import org.nikok.hextant.*
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.EditorViewFactory
import org.nikok.hextant.core.fx.keyword
import org.nikok.hextant.core.fx.operator

abstract class CompoundEditorControl(
    private val platform: HextantPlatform,
    private val build: Vertical.() -> Unit
) : EditorControl<VBox>() {
    override fun createDefaultRoot(): VBox = Vertical().also(build)

    interface Compound {
        fun view(editable: Editable<*>): EditorControl<*>

        fun space(): Label

        fun keyword(name: String): Label

        fun operator(str: String): Label
    }

    inner class Vertical : VBox(), Compound {
        override fun view(editable: Editable<*>) = view(editable, this, platform)

        override fun space() = space(this)

        override fun keyword(name: String): Label = keyword(name, this)

        override fun operator(str: String): Label = operator(str, this)

        fun line(build: Horizontal.() -> Unit): Horizontal {
            val horizontal = Horizontal().apply(build)
            children.add(horizontal)
            return horizontal
        }

        fun indented(build: Vertical.() -> Unit): HBox {
            val indent = Label("  ")
            val v = Vertical().apply(build)
            val indented = HBox(indent, v)
            children.add(indented)
            return indented
        }
    }

    inner class Horizontal : HBox(), Compound {
        override fun view(editable: Editable<*>): EditorControl<*> = view(editable, this, platform)

        override fun space() = space(this)

        override fun keyword(name: String): Label = keyword(name, this)

        override fun operator(str: String): Label = operator(str, this)
    }

    companion object {
        private fun view(
            editable: Editable<*>,
            pane: Pane,
            platform: HextantPlatform
        ): EditorControl<*> {
            val c = platform[Public, EditorViewFactory].getFXView(editable)
            pane.children.add(c)
            return c
        }

        private fun keyword(name: String, pane: Pane): Label {
            val l = keyword(name)
            pane.children.add(l)
            return l
        }

        private fun operator(name: String, pane: Pane): Label {
            val l = operator(name)
            pane.children.add(l)
            return l
        }

        private fun space(pane: Pane): Label {
            val l = Label(" ")
            pane.children.add(l)
            return l
        }

        operator fun invoke(
            editable: Editable<*>,
            editor: Editor<*>,
            platform: HextantPlatform,
            build: Vertical.() -> Unit
        ): CompoundEditorControl = object : CompoundEditorControl(platform, build) {
            init {
                initialize(editable, editor, platform)
            }
        }
    }
}