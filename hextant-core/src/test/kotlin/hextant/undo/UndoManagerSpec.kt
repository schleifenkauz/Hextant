package hextant.undo

import hextant.test.`false`
import hextant.test.`true`
import hextant.test.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

internal object UndoManagerSpec : Spek({
    given("a new undo manager") {
        val subject = UndoManager.newInstance()
        fun testCanUndo(expected: Boolean) {
            if (expected) {
                it("should be able to undo") {
                    subject.canUndo shouldBe `true`
                }
            } else {
                it("should not be able to undo") {
                    subject.canUndo shouldBe `false`
                }
            }
        }

        fun testCanRedo(expected: Boolean) {
            if (expected) {
                it("should be able to redo") {
                    subject.canRedo shouldBe `true`
                }
            } else {
                it("should not be able to redo") {
                    subject.canRedo shouldBe `false`
                }
            }
        }
        testCanRedo(false)
        testCanUndo(false)
        val e1 = AnEdit()
        on("pushing an edit") {
            subject.record(e1)
            it("should be able to undo") {
                testCanUndo(true)
            }
            it("should not be able to redo") {
                testCanRedo(false)
            }
        }
    }
})