/**
 * @author Nikolaus Knop
 */

package hextant.test

import bundles.createBundle
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import hextant.context.Context
import hextant.core.Editor
import hextant.core.EditorView
import hextant.core.editor.AbstractEditor
import reaktive.value.ReactiveValue
import reaktive.value.reactiveValue

inline fun <reified V : EditorView> mockView(target: Editor<*>): V = mock {
    on { target }.doReturn(target)
    on { arguments }.doReturn(createBundle())
}

fun mockEditor(context: Context) = object : AbstractEditor<Unit, EditorView>(context) {
    override val result: ReactiveValue<Unit> = reactiveValue(Unit)
}