/**
 * @author Nikolaus Knop
 */

package hextant.test

import bundles.createBundle
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import hextant.core.Editor
import hextant.core.EditorView
import hextant.core.editor.AbstractEditor
import kotlinx.serialization.json.JsonElement
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue

inline fun <reified V : EditorView> mockView(target: Editor<*>): V = mock {
    on { this.target }.doReturn(target)
    on { arguments }.doReturn(createBundle())
}

fun mockEditor() = object : AbstractEditor<Unit, EditorView>() {
    override val result: ReactiveValue<Unit> = reactiveValue(Unit)

    override fun serialize(): JsonElement {
        TODO("Not yet implemented")
    }

    override fun deserialize(element: JsonElement) {
        TODO("Not yet implemented")
    }
}