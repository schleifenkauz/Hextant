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
        fun view(editable: Editable<*>)

        fun space()

        fun keyword(name: String)

        fun operator(str: String)
    }

    inner class Vertical : VBox(), Compound {
        override fun view(editable: Editable<*>) {
            view(editable, this, platform)
        }

        override fun space() {
            space(this)
        }

        override fun keyword(name: String) {
            keyword(name, this)
        }

        override fun operator(str: String) {
            operator(str, this)
        }

        fun line(build: Horizontal.() -> Unit) {
            children.add(Horizontal().apply(build))
        }

        fun indented(build: Vertical.() -> Unit) {
            val indent = Label()
            val v = Vertical().apply(build)
            val indented = HBox(indent, v)
            children.add(indented)
        }
    }

    inner class Horizontal : HBox(), Compound {
        override fun view(editable: Editable<*>) {
            view(editable, this, platform)
        }

        override fun space() {
            space(this)
        }

        override fun keyword(name: String) {
            keyword(name, this)
        }

        override fun operator(str: String) {
            operator(str, this)
        }
    }

    companion object {
        private fun view(
            editable: Editable<*>,
            pane: Pane,
            platform: HextantPlatform
        ) {
            val c = platform[Public, EditorViewFactory].getFXView(editable)
            pane.children.add(c)
        }

        private fun keyword(name: String, pane: Pane) {
            val l = keyword(name)
            pane.children.add(l)
        }

        private fun operator(name: String, pane: Pane) {
            val l = operator(name)
            pane.children.add(l)
        }

        private fun space(pane: Pane) {
            pane.children.add(Label())
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