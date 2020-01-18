/**
 *@author Nikolaus Knop
 */

package hextant.core.editor

import com.natpryce.hamkrest.should.shouldMatch
import hextant.*
import hextant.core.view.FilteredTokenEditorView
import hextant.serial.HextantSerialContext
import hextant.test.*
import io.mockk.mockk
import io.mockk.verify
import kserial.KSerial
import kserial.readTyped
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import org.junit.jupiter.api.assertThrows
import reaktive.event.EventStream
import reaktive.value.now
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object FilteredTokenEditorSpec : Spek({
    GIVEN("a FilteredTokenEditor") {
        describe("basic interactions") {
            val ctx = testingContext()
            val e = Test(ctx, "123")
            val handler = mockk<(EventStream<*>, Any?) -> Unit>(relaxed = true)
            val o1 = e.beganChange.subscribe(handler)
            val o2 = e.abortedChange.subscribe(handler)
            val o3 = e.commitedChange.subscribe(handler)
            val v = mockk<FilteredTokenEditorView>(relaxed = true)
            group("initially") {
                it("should take the initial text") {
                    e.text.now shouldEqual "123"
                }
                it("should compile the initial text") {
                    e.result.now shouldEqual ok(123)
                    e.intermediateResult.now shouldEqual ok(123)
                }
                it("should not be editable") {
                    e.editable.now shouldBe `false`
                }
                test("setting the text should throw an exception") {
                    assertThrows<IllegalStateException> { e.setText("1") }
                }
                test("aborting should have no effect") {
                    e.abortChange()
                }
                test("committing should have no effect") {
                    e.commitChange()
                }
            }
            on("adding a view") {
                e.addView(v)
                it("should display the text") {
                    verify { v.displayText("123") }
                }
            }
            on("beginning a change") {
                e.beginChange()
                o1 and o2 and o3
                it("should be editable") {
                    e.editable.now shouldBe `true`
                }
                it("should fire the event") {
                    verify { handler.invoke(any(), any()) }
                }
                it("should notify the view") {
                    verify { v.beginChange() }
                }
            }
            on("setting the text") {
                e.setText("invalid")
                it("should update the intermediate result") {
                    e.intermediateResult.now shouldMatch err
                }
                it("should not update the result") {
                    e.result.now shouldEqual ok(123)
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
                    e.intermediateResult.now shouldEqual ok(123)
                }
                it("should not editable") {
                    e.editable.now shouldBe `false`
                }
                it("should fire the event") {
                    verify { handler.invoke(e.abortedChange, Unit) }
                }
                it("should notify the view") {
                    verify { v.endChange() }
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
                    verify { v.beginChange() }
                    verify { v.displayText("456") }
                }

            }
            on("committing the change") {
                e.commitChange()
                it("should update the result") {
                    e.result.now shouldEqual ok(456)
                }
                it("should not be editable") {
                    e.editable.now shouldBe `false`
                }
                it("should fire the event") {
                    verify { handler.invoke(e.commitedChange, "456") }
                }
                it("should notify the view") {
                    verify { v.endChange() }
                }
            }
        }
        describe("undo and redo") {
            //TODO
        }
        describe("serialization") {
            val ser = KSerial.newInstance()
            val platform = HextantPlatform.forTesting
            val context = Context.newInstance(platform)
            val ctx = HextantSerialContext(platform, classLoader = FilteredTokenEditorSpec::class.java.classLoader)
            val baos = ByteArrayOutputStream()
            val out = ser.createOutput(baos, ctx)
            val e = Test(context, "123")
            e.beginChange()
            out.writeObject(e)
            val input = ser.createInput(ByteArrayInputStream(baos.toByteArray()), ctx)
            test("serializing and deserializing should fully reconstruct object state but should abort changes") {
                val new = input.readTyped<Test>()
                e.editable.now shouldBe `false`
                new.editable.now shouldBe `false`
                new.text.now shouldEqual "123"
                new.intermediateResult.now shouldEqual ok(123)
                new.result.now shouldEqual ok(123)
            }
        }
        describe("copying") {
            val ctx = testingContext()
            val original = Test(ctx, "123")
            test("copying should fully reconstruct object state but should abort changes") {
                val copy = original.copy()
                original.editable.now shouldBe `false`
                copy.editable.now shouldBe `false`
                copy.text.now shouldEqual "123"
                copy.intermediateResult.now shouldEqual ok(123)
                copy.result.now shouldEqual ok(123)
            }
        }
    }
}) {
    class Test(context: Context, initialText: String = "") : FilteredTokenEditor<Int>(context, initialText) {
        override fun compile(token: String): CompileResult<Int> = token.toIntOrNull().okOrErr { "Invalid int literal" }
    }
}