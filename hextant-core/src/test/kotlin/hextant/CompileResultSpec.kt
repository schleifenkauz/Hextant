package hextant

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import matchers.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

object CompileResultSpec : Spek({
    describe("isOk") {
        on("Err") {
            it("returns false") {
                Err("some message").isOk shouldEqual false
            }
        }
        on("ChildErr") {
            it("returns false") {
                ChildErr.isOk shouldEqual false
            }
        }
        on("Ok") {
            it("returns true") {
                ok(123).isOk shouldEqual true
            }
        }
    }
    describe("isErr") {
        on("Ok") {
            it("returns false") {
                ok(123).isErr shouldEqual false
            }
        }
        on("ChildErr") {
            it("returns false") {
                ChildErr.isErr shouldEqual false
            }
        }
        on("Err") {
            it("returns true") {
                Err("Some error").isErr shouldEqual true
            }
        }
    }
    describe("isChildErr") {
        on("Ok") {
            it("returns false") {
                ok(123).isChildErr shouldEqual false
            }
        }
        on("Err") {
            it("returns false") {
                Err("Some error").isChildErr shouldEqual false
            }
        }
        on("ChildErr") {
            it("returns true") {
                ChildErr.isChildErr shouldEqual true
            }
        }
    }
    describe("isError") {
        on("Ok") {
            it("returns false") {
                ok(123).isError shouldEqual false
            }
        }
        on("Err") {
            it("returns true") {
                Err("Some error").isError shouldEqual true
            }
        }
        on("ChildErr") {
            it("returns true") {
                ChildErr.isError shouldEqual true
            }
        }
    }
    describe("map") {
        on("Ok") {
            it("maps the result") {
                ok(123).map { it * 2 } shouldEqual ok(246)
            }
        }
        on("Err") {
            it("returns the error") {
                err<Int>("some message").map { it * 2 } shouldBe err
            }
        }
        on("ChildErr") {
            it("returns the error") {
                childErr<Int>().map { it * 2 } shouldBe childErr
            }
        }
    }
    describe("flatMap") {
        on("Ok") {
            it("returns the result of the function") {
                ok(123).flatMap { Ok(it * 2) } shouldEqual ok(246)
            }
        }
        on("Err") {
            it("returns the error") {
                err<Int>("some error message").flatMap { Ok(it * 2) } shouldBe err
            }
        }
        on("Err") {
            it("returns the error") {
                childErr<Int>().flatMap { Ok(it * 2) } shouldBe childErr
            }
        }
    }
    describe("orElse") {
        on("Ok") {
            it("returns the ok") {
                ok(123).or(ok(234)) shouldEqual ok(123)
            }
        }
        on("Err") {
            it("returns the alternative") {
                err<Int>("some error").or(ok(345)) shouldEqual ok(345)
            }
        }
        on("Err") {
            it("returns the alternative") {
                childErr<Int>().or(err("Alternative error")) shouldEqual err("Alternative error")
            }
        }
    }
    describe("okOr") {
        on("null value") {
            it("returns the default") {
                null.okOr { err("Null value") } shouldBe err
            }
        }
        on("non-null value") {
            it("returns an ok around the value") {
                1.okOr { err("Null value") } shouldBe ok
            }
        }
    }
    describe("ifErr") {
        on("Ok") {
            it("returns the value") {
                ok(1).ifErr { throw AssertionError() }
            }
        }
        on("Err") {
            it("returns the default") {
                err<Int>("Some message").orNull() shouldEqual null
            }
        }
        on("ChildErr") {
            it("executes the default") {
                { childErr<Unit>().force() } shouldMatch throws<IllegalArgumentException>()
            }
        }
    }
    describe("compile") {
        on("flow without errors") {
            it("should compute the result") {
                compile {
                    val (x) = ok(1)
                    val (y) = ok(2)
                    ok(x + y)
                } shouldEqual ok(3)
            }
        }
        on("flow with errors") {
            it("should return the first error") {
                compile<Int> {
                    ok(1).orTerminate()
                    val (x) = childErr<Int>() //Cannot rename to _ because it would not terminate execution
                    err<Int>("Message").orTerminate()
                    throw AssertionError("Should not be reached")
                } shouldBe childErr
            }
        }
    }
})