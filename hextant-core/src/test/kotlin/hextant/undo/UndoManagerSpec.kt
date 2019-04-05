package hextant.undo

import hextant.test.matchers.*
import hextant.undo.UndoManagerSpec.execute
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object UndoManagerSpec : Spek({
    execute(UndoManagerImpl())
}) {
    /**
     * Execute the spec on the specified [subject]
     * * The subject must not have been used before
     */
    fun Spec.execute(subject: UndoManager) {
        given("a new undo manager") {
            fun testCanUndo(expected: Boolean) {
                if (expected) it("should be able to undo") {
                    subject.canUndo shouldBe `true`
                } else it("should not be able to undo") {
                    subject.canUndo shouldBe `false`
                }
            }

            fun testCanRedo(expected: Boolean) {
                if (expected) it("should be able to redo") {
                    subject.canRedo shouldBe `true`
                } else it("should not be able to redo") {
                    subject.canRedo shouldBe `false`
                }
            }
            testCanRedo(false)
            testCanUndo(false)
            val e1 = AnEdit()
            on("pushing an edit") {
                subject.push(e1)
                it("should be able to undo") {
                    testCanUndo(true)
                }
                it("should not be able to redo") {
                    testCanRedo(false)
                }
            }
        }
    }
}