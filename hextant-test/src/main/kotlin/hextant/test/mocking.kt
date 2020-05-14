/**
 * @author Nikolaus Knop
 */

package hextant.test

import bundles.createBundle
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import hextant.EditorView

inline fun <reified V : EditorView> mockView(): V = mock {
    on { target }.doReturn(Any())
    on { arguments }.doReturn(createBundle())
}
