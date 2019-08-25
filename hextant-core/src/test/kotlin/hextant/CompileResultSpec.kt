package hextant

import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import hextant.test.*
import org.jetbrains.spek.api.Spek

object CompileResultSpec : Spek({
    DESCRIBE("isOk") {
        ON("Err") {
            IT("returns false") {
                Err("some message").isOk shouldEqual false
            }
        }
        ON("ChildErr") {
            IT("returns false") {
                ChildErr.isOk shouldEqual false
            }
        }
        ON("Ok") {
            IT("returns true") {
                ok(123).isOk shouldEqual true
            }
        }
    }
    DESCRIBE("isErr") {
        ON("ok") {
            IT("returns false") {
                ok(123).isErr shouldEqual false
            }
        }
        ON("ChildErr") {
            IT("returns false") {
                ChildErr.isErr shouldEqual false
            }
        }
        ON("err") {
            IT("returns true") {
                Err("Some error").isErr shouldEqual true
            }
        }
    }
    DESCRIBE("isChildErr") {
        ON("ok") {
            IT("returns false") {
                ok(123).isChildErr shouldEqual false
            }
        }
        ON("Err") {
            IT("returns false") {
                Err("Some error").isChildErr shouldEqual false
            }
        }
        ON("ChildErr") {
            IT("returns true") {
                ChildErr.isChildErr shouldEqual true
            }
        }
    }
    DESCRIBE("is error") {
        ON("ok") {
            IT("returns false") {
                ok(123).isError shouldEqual false
            }
        }
        ON("err") {
            IT("returns true") {
                Err("Some error").isError shouldEqual true
            }
        }
        ON("child err") {
            IT("returns true") {
                ChildErr.isError shouldEqual true
            }
        }
    }
    DESCRIBE("map") {
        ON("ok") {
            IT("maps the result") {
                ok(123).map { it * 2 } shouldEqual ok(246)
            }
        }
        ON("err") {
            IT("returns the error") {
                err<Int>("some message").map { it * 2 } shouldBe err
            }
        }
        ON("childErr") {
            IT("returns the error") {
                childErr<Int>().map { it * 2 } shouldBe childErr
            }
        }
    }
    DESCRIBE("flatMap") {
        ON("ok") {
            IT("returns the result of the function") {
                ok(123).flatMap { Ok(it * 2) } shouldEqual ok(246)
            }
        }
        ON("err") {
            IT("returns the error") {
                err<Int>("some error message").flatMap { Ok(it * 2) } shouldBe err
            }
        }
        ON("err") {
            IT("returns the error") {
                childErr<Int>().flatMap { Ok(it * 2) } shouldBe childErr
            }
        }
    }
    DESCRIBE("orElse") {
        ON("OK") {
            IT("returns the ok") {
                ok(123).or(ok(234)) shouldEqual ok(123)
            }
        }
        ON("Err") {
            IT("returns the alternative") {
                err<Int>("some error").or(ok(345)) shouldEqual ok(345)
            }
        }
        ON("Err") {
            IT("returns the alternative") {
                childErr<Int>().or(err("Alternative error")) shouldEqual err("Alternative error")
            }
        }
    }
    DESCRIBE("okOr") {
        ON("null value") {
            IT("returns the default") {
                null.okOr { err("Null value") } shouldBe err
            }
        }
        ON("non-null value") {
            IT("returns an ok around the value") {
                1.okOr { err("Null value") } shouldBe ok
            }
        }
    }
    DESCRIBE("ifErr") {
        ON("OK") {
            IT("returns the value") {
                ok(1).ifErr { throw AssertionError() }
            }
        }
        ON("err") {
            IT("returns the default") {
                err<Int>("Some message").orNull() shouldEqual null
            }
        }
        ON("childErr") {
            IT("executes the default") {
                { childErr<Unit>().force() } shouldMatch throws<IllegalArgumentException>()
            }
        }
    }
    DESCRIBE("compile") {
        ON("flow without errors") {
            IT("should compute the result") {
                compile {
                    val (x) = ok(1)
                    val (y) = ok(2)
                    ok(x + y)
                } shouldEqual ok(3)
            }
        }
        ON("flow with errors") {
            IT("should return the first error") {
                compile<Int> {
                    ok(1).orTerminate()
                    childErr<Int>().orTerminate()
                    err<Int>("Message").orTerminate()
                    throw AssertionError("Should not be reached")
                } shouldBe childErr
            }
        }
    }
})