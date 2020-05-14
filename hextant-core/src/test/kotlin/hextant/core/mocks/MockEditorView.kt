package hextant.core.mocks

import bundles.Bundle
import bundles.createBundle
import hextant.EditorView
import hextant.ViewGroup

internal class MockEditorView : EditorView {
    override val arguments: Bundle = createBundle()

    override val target: Any
        get() = Any()

    override fun deselect() {

    }

    override fun focus() {
        TODO("not implemented")
    }

    override val group: ViewGroup<*>
        get() = TODO("not implemented")
}