package hextant.undo

import hextant.test.*
import hextant.undo.UndoManagerSpec.execute
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.Spec

internal object UndoManagerSpec : Spek({
    execute(UndoManagerImpl())
}) {
    /**
     * Execute the spec on the specified [subject]
     * * The subject must not have been used before
     */
    fun Spec.execute(subject: UndoManager) {
        GIVEN("a new undo manager") {
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
    }
}