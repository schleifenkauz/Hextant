/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import hextant.Context
import hextant.copy
import hextant.core.view.FilteredTokenEditorView
import hextant.serial.SerialProperties.deserializationContext
import hextant.serial.SerialProperties.serialContext
import hextant.serial.makeRoot
import hextant.test.*
import io.mockk.mockk
import io.mockk.verify
import kserial.KSerial
import kserial.readTyped
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import reaktive.event.EventStream
import reaktive.value.now
import validated.*
import validated.Validated.Valid
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object FilteredTokenEditorSpec : Spek({
    given("a FilteredTokenEditor") {
        describe("basic interactions") {
            val ctx = testingContext()
            val e = Test(ctx, "123")
            e.makeRoot()
            val handler = mockk<(EventStream<*>, Any?) -> Unit>(relaxed = true)
            val o1 = e.beganChange.observe(handler)
            val o2 = e.abortedChange.observe(handler)
            val o3 = e.commitedChange.observe(handler)
            val v = mockk<FilteredTokenEditorView>(relaxed = true)
            group("initially") {
                it("should take the initial text") {
                    e.text.now shouldEqual "123"
                }
                it("should compile the initial text") {
                    e.intermediateResult.now shouldEqual valid(123)
                }
                it("it should initialize the result with a child error") {
                    e.result.now shouldEqual invalidComponent()
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
                    e.result.now shouldEqual Valid(123)
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
                    e.intermediateResult.now shouldBe invalid
                }
                it("should not update the result") {
                    e.result.now shouldEqual valid(123)
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
                    e.intermediateResult.now shouldEqual valid(123)
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
                    e.result.now shouldEqual valid(456)
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
        describe("undo and redo") {
            //TODO
        }
        describe("serialization") {
            val ser = KSerial.newInstance()
            val context = testingContext()
            val baos = ByteArrayOutputStream()
            val out = ser.createOutput(baos, context[serialContext])
            val e = Test(context, "123")
            e.makeRoot()
            e.beginChange()
            out.writeObject(e)
            val input = ser.createInput(ByteArrayInputStream(baos.toByteArray()), context[serialContext])
            input.bundle[deserializationContext] = context
            val new = input.readTyped<Test>()
            on("serializing and deserializing") {
                test("the deserialized editor should not be editable") {
                    new.editable.now shouldBe `false`
                }
                test("the text should be reconstructed") {
                    new.text.now shouldEqual "123"
                }
                test("the intermediate result should be recompiled") {
                    new.intermediateResult.now shouldEqual valid(123)
                }
                test("the result should be initialized") {
                    new.result.now shouldEqual valid(123)
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
                    copy.intermediateResult.now shouldEqual valid(123)
                }
                test("the result should be copied") {
                    copy.result.now shouldEqual valid(123)
                }
            }
        }
    }
}) {
    class Test(context: Context, initialText: String = "") : FilteredTokenEditor<Int>(context, initialText) {
        override fun compile(token: String): Validated<Int> =
            token.toIntOrNull().validated { invalid("Invalid int literal") }
    }
}