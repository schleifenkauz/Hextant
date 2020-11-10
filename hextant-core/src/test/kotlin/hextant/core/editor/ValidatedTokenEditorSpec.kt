/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.context.Context
import hextant.core.view.ValidatedTokenEditorView
import hextant.serial.makeRoot
import hextant.test.*
import io.mockk.mockk
import io.mockk.verify
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.event.EventStream
import reaktive.value.now

object ValidatedTokenEditorSpec : Spek({
    given("a FilteredTokenEditor") {
        describe("basic interactions") {
            val ctx = testingContext()
            val e = Test(ctx, "123")
            e.makeRoot()
            val handler = mockk<(EventStream<*>, Any?) -> Unit>(relaxed = true)
            val o1 = e.beganChange.observe(handler)
            val o2 = e.abortedChange.observe(handler)
            val o3 = e.commitedChange.observe(handler)
            val v = mockk<ValidatedTokenEditorView>(relaxed = true)
            group("initially") {
                it("should take the initial text") {
                    e.text.now shouldEqual "123"
                }
                it("should compile the initial text") {
                    e.intermediateResult.now shouldEqual 123
                }
                it("it should initialize the result with null") {
                    e.result.now shouldEqual null
                }
                it("should not editable") {
                    e.editable.now shouldBe `true`
                }
            }
            on("adding a view") {
                e.addView(v)
                it("should make the view editable") {
                    verify { v.setEditable(true) }
                }
                it("should display the text") {
                    verify { v.displayText("123") }
                }
            }
            on("committing") {
                e.commitChange()
                it("should fire the change") {
                    verify { handler.invoke(e.commitedChange, "123") }
                }
                it("should notify the view") {
                    verify { v.setEditable(false) }
                }
                it("should set the result") {
                    e.result.now shouldEqual 123
                }
                it("should not be editable") {
                    e.editable.now shouldBe `false`
                }
            }
            on("beginning a change") {
                e.beginChange()
                o1 and o2 and o3
                it("should be editable") {
                    e.editable.now shouldBe `true`
                }
                it("should fire the event") {
                    verify { handler.invoke(e.beganChange, Unit) }
                }
                it("should notify the view") {
                    verify { v.setEditable(true) }
                }
            }
            on("setting the text") {
                e.setText("invalid")
                it("should update the intermediate result") {
                    e.intermediateResult.now shouldBe `null`
                }
                it("should not update the result") {
                    e.result.now shouldEqual 123
                }
                it("should update the text") {
                    e.text.now shouldEqual "invalid"
                }
                it("should notify the view") {
                    verify { v.displayText("invalid") }
                }
            }
            on("aborting") {
                e.abortChange()
                it("should revert the text") {
                    e.text.now shouldEqual "123"
                }
                it("should revert the intermediate result") {
                    e.intermediateResult.now shouldEqual 123
                }
                it("should not editable") {
                    e.editable.now shouldBe `false`
                }
                it("should fire the event") {
                    verify { handler.invoke(e.abortedChange, Unit) }
                }
                it("should notify the view") {
                    verify { v.setEditable(false) }
                }
            }
            on("beginning a new change and setting the text") {
                e.beginChange()
                e.setText("456")
                it("should set text") {
                    e.text.now shouldEqual "456"
                }
                it("should fire the events") {
                    verify { handler.invoke(e.beganChange, Unit) }
                }
                it("should notify the view") {
                    verify { v.setEditable(true) }
                    verify { v.displayText("456") }
                }

            }
            on("committing the change") {
                e.commitChange()
                it("should update the result") {
                    e.result.now shouldEqual 456
                }
                it("should not be editable") {
                    e.editable.now shouldBe `false`
                }
                it("should fire the event") {
                    verify { handler.invoke(e.commitedChange, "456") }
                }
                it("should notify the view") {
                    verify { v.setEditable(false) }
                }
            }
        }
        describe("copying") {
            val ctx = testingContext()
            val original = Test(ctx, "123")
            val copy = original.copy()
            on("copying") {
                test("the copy should not be editable") {
                    copy.editable.now shouldBe `false`
                }
                test("the text should be copied") {
                    copy.text.now shouldEqual "123"
                }
                test("the intermediate result should be copied") {
                    copy.intermediateResult.now shouldEqual 123
                }
                test("the result should be copied") {
                    copy.result.now shouldEqual 123
                }
            }
        }
    }
}) {
    class Test(context: Context, initialText: String = "") : ValidatedTokenEditor<Int>(context, initialText) {
        override fun wrap(token: String): Int? = token.toIntOrNull()
    }
}