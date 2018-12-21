package hextant.sample.ast

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import hextant.sample.ast.IntOperator.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

internal object IntOperatorSpec : Spek({
    describe("IntOperator.Plus") {
        it("should compute the sum of the operands") {
            Plus.operate(1, 2) shouldMatch equalTo(3)
        }
    }
    describe("IntOperator.Minus") {
        it("should compute the sum of the operands") {
            Minus.operate(1, 2) shouldMatch equalTo(-1)
        }
    }
    describe("IntOperator.Times") {
        it("should compute the sum of the operands") {
            Times.operate(1, 2) shouldMatch equalTo(2)
        }
    }
    describe("IntOperator.Div") {
        it("should compute the sum of the operands") {
            Div.operate(4, 2) shouldMatch equalTo(2)
        }
    }

})
