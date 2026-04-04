/**
 *@author Nikolaus Knop
 */

package hextant.inspect

import bundles.createBundle
import bundles.set
import com.natpryce.hamkrest.absent
import com.sun.javafx.application.PlatformImpl
import hextant.context.EditorControlGroup
import hextant.context.createControl
import hextant.expr.editor.ExprExpander
import hextant.expr.editor.IntLiteralEditor
import hextant.test.shouldBe
import hextant.test.testingContext
import fxutils.undo.NoUndoManager
import fxutils.undo.UndoManager
import hextant.completion.CompletionStrategy
import hextant.completion.CompoundCompleter
import hextant.core.editor.Expander
import hextant.core.view.ExpanderControl
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import reaktive.getValue
import reaktive.value.ReactiveBoolean
import reaktive.value.now
import reaktive.value.reactiveValue
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

class InspectionMemoryLeakTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun startupPlatform() {
            PlatformImpl.startup {}
        }
    }

    @Test
    fun `get problems of editor causes no memory leak`() {
        class Inspected(val error: ReactiveBoolean)

        var e: Inspected? = Inspected(reactiveValue(true))
        val i = Inspections.newInstance()
        i.registerInspection<Inspected> {
            id = "test-inspection"
            description = "An inspection"
            message { "error" }
            isSevere(true)
            preventingThat { inspected.error }
            addFix {
                description = "Fix"
                fixingBy { println(inspected) }
            }
        }
        i.getProblems(e!!)
        val r = WeakReference(e)

        e = null
        gc()
        r.get() shouldBe absent()
    }

    @Test
    fun `constructing view of editor causes no memory leak`() {
        val ctx = testingContext()
        var e: IntLiteralEditor? = IntLiteralEditor()
        val v = WeakReference(ctx.createControl(e!!), ReferenceQueue())
        val r = WeakReference(e)
        e = null
        gc()
        r.get() shouldBe absent()
        v.get() shouldBe absent()
    }

    @Test
    fun `expanding and resetting expander causes no leak`() {
        val ctx = testingContext()
        ctx[UndoManager] = NoUndoManager
        val exp = ExprExpander()
        exp.initialize(ctx)
        val c = CompoundCompleter<Expander<*, *>, Any> {}
        c.addCompleter(ExprExpander.config.completer(CompletionStrategy.simple))
        val view =     //    c.addCompleter(SpecialNumbers)
            ExpanderControl(exp, createBundle(), c)
        exp.setText("1")
        exp.expand()
        val e by WeakReference(exp.editor.now!!)
        val v by WeakReference(ctx[EditorControlGroup].getViewOf(e!!))
        exp.reset()
        gc()
        e shouldBe absent()
        v shouldBe absent()
        println(view)
    }

    private fun gc() {
        repeat(10) {
            Thread.sleep(10)
            System.gc()
        }
    }
}