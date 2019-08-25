package hextant.undo

import hextant.test.*
import org.jetbrains.spek.api.Spek

internal object UndoManagerSpec : Spek({
    GIVEN("a new undo manager") {
        val subject = UndoManager.newInstance()
        fun testCanUndo(expected: Boolean) {
            if (expected) IT("should be able to undo") {
                subject.canUndo shouldBe `true`
            } else IT("should not be able to undo") {
                subject.canUndo shouldBe `false`
            }
        }

        fun testCanRedo(expected: Boolean) {
            if (expected) IT("should be able to redo") {
                subject.canRedo shouldBe `true`
            } else IT("should not be able to redo") {
                subject.canRedo shouldBe `false`
            }
        }
        testCanRedo(false)
        testCanUndo(false)
        val e1 = AnEdit()
        ON("pushing an edit") {
            subject.push(e1)
            IT("should be able to undo") {
                testCanUndo(true)
            }
            IT("should not be able to redo") {
                testCanRedo(false)
            }
        }
    }
})