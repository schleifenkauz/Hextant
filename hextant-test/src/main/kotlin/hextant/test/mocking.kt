/**
 * @author Nikolaus Knop
 */

package hextant.test

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import hextant.EditorView
import hextant.bundle.createBundle

inline fun <reified V : EditorView> mockView(): V = mock {
    on { target }.doReturn(Any())
    on { arguments }.doReturn(createBundle())
}
