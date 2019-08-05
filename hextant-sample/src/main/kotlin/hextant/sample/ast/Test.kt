/**
 * @author Nikolaus Knop
 */

package hextant.sample.ast

import hextant.CompileResult
import hextant.codegen.Token
import hextant.core.TokenType
import hextant.ok
import hextant.sample.editor.TestEditor

@Token(pkg = "hextant.sample.editor", name = "TestEditor")
class Test {
    companion object: TokenType<Test> {
        override fun compile(token: String): CompileResult<Test> = ok(Test())
    }
}

fun main() {
    Test().javaClass.classLoader.loadClass("hextant.sample.editor.TestEditor")
}