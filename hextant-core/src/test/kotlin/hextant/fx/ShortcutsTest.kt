/**
 *@author Nikolaus Knop
 */

package hextant.fx

import hextant.fx.ModifierValue.DOWN
import hextant.fx.ModifierValue.MAYBE
import hextant.fx.ShortcutsTest.event
import hextant.test.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyEvent
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.junit.jupiter.api.assertThrows
import java.text.ParseException

object ShortcutsTest : Spek({
    describe("parsing shortcuts") {
        val cases = listOf(
            "A" to shortcut(A),
            "Ctrl+A" to shortcut(A) { control(DOWN) },
            " Ctrl +   B " to shortcut(B) { control(DOWN) },
            "alt? + C" to shortcut(C) { alt(MAYBE) }
        )
        for ((str, shortcut) in cases) {
            test("parse $str") {
                str.shortcut shouldEqual shortcut
            }
        }
    }
    describe("failures while parsing") {
        val cases = listOf(
            "gibberish" to "invalid token",
            "+ Ctrl + A" to "start with +",
            "    " to "blank",
            "Ctrl + Alt? + shift" to "no main character",
            "Ctrl + A + " to "end with +"
        )
        for ((str, cause) in cases) {
            test("$cause fails") {
                assertThrows<ParseException> { str.shortcut }
            }
        }
    }
    describe("matching key events") {
        val cases = listOf(
            "A" to event(A) to true,
            "A" to event(A, control = true) to false,
            "Ctrl + A" to event(A, control = true) to true,
            "ctrl + A" to event(A, control = false) to false,
            "CTrl? + A" to event(A, control = false) to true,
            "CtRl? + A" to event(A, control = true) to true,
            "alT + DELETE" to event(DELETE, alt = true) to true,
            "ctrl? + alt? + shift? + A" to event(A, alt = true, shift = true) to true,
            "ctrl? + alt? + shift? + A" to event(B, alt = true) to false
        )
        for ((case, result) in cases) {
            val (shortcut, event) = case
            if (result) {
                test("$shortcut matches $event") {
                    shortcut.shortcut.matches(event) shouldBe `true`
                }
            } else {
                test("$shortcut does not match $event") {
                    shortcut.shortcut.matches(event) shouldBe `false`
                }
            }
        }
    }
}) {
    fun event(code: KeyCode, control: Boolean = false, shift: Boolean = false, alt: Boolean = false) =
        KeyEvent(KeyEvent.KEY_RELEASED, "", "", code, shift, control, alt, false)
}