/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import com.natpryce.hamkrest.absent
import hextant.HextantPlatform
import hextant.core.mocks.MockEditor
import hextant.test.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.nikok.kref.weak

class InspectionMemoryLeakTest {
    @Test
    fun `get problems of editor causes no memory leak`() {
        val c = HextantPlatform.forTesting
        val e by weak(MockEditor(c))
        val i = Inspections.newInstance()
        i.getProblems(e!!)
        System.gc()
        e shouldBe absent()
    }
}