/**
 * @author Nikolaus Knop
 */

package hextant.mocking

import com.nhaarman.mockitokotlin2.*
import hextant.EditorView

inline fun <reified V : EditorView> viewMock(stubbing: KStubbing<V>.() -> Unit = {}): V = mock { v ->
    on { onGuiThread(any()) }.then { it.getArgument<() -> Unit>(0).invoke() }
    stubbing()
}