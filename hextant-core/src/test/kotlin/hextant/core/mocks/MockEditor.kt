package hextant.core.mocks

import hextant.*
import hextant.base.AbstractEditor
import reaktive.value.reactiveValue

internal class MockEditor(
    context: Context
) : AbstractEditor<Unit, Any>(context) {
    override val result: EditorResult<Unit> = reactiveValue(ok(Unit))
}