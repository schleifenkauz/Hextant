package hextant.undo

import hextant.test.*
import org.jetbrains.spek.api.Spek

internal object AbstractEditSpec : Spek({
    GIVEN("an edit") {
        val e = AnEdit()
        IT("should be able to undo") {
            e.canUndo shouldBe `true`
        }
        IT("shouldn't be able to redo") {
            e.canRedo shouldBe `false`
        }
        ON("redoing it") {
            IT("should throw an illegal state exception") {
                { e.redo() }.shouldThrow<IllegalStateException>()
            }
            IT("should not actually redo") {
                e.undone shouldBe `false`
            }
            IT("should still not be able to redo") {
                e.canRedo shouldBe `false`
            }
            IT("should still be able to undo") {
                e.canUndo shouldBe `true`
            }
        }
        ON("undoing it") {
            e.undo()
            IT("should actually undo") {
                e.undone shouldBe `true`
            }
            IT("should be able to redo") {
                e.canRedo shouldBe `true`
            }
            IT("should not be able to undo") {
                e.canUndo shouldBe `false`
            }
        }
        ON("undoing it") {
            IT("should throw an illegal state exception") {
                { e.undo() }.shouldThrow<IllegalStateException>()
            }
            IT("should not actually undo") {
                e.undone shouldBe `true`
            }
            IT("should still not be able to undo") {
                e.canUndo shouldBe `false`
            }
            IT("should still be able to redo") {
                e.canRedo shouldBe `true`
            }
        }
        ON("redoing it") {
            e.redo()
            IT("should actually redo") {
                e.undone shouldBe `false`
            }
            IT("should be able to undo") {
                e.canUndo shouldBe `true`
            }
            IT("should not be able to redo") {
                e.canRedo shouldBe `false`
            }
        }
    }
})
