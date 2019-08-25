/**
 * @author Nikolaus Knop
 */

package hextant.sample.ast

import hextant.CompileResult
import hextant.codegen.*
import hextant.core.TokenType
import hextant.ok
import hextant.sample.ast.editor.*

object AltExpanderDelegator : ExpanderConfigurator<AltEditor<Alt>>({
    registerConstant("token") { TestTokenEditor(it) }
    registerConstant("comp") { CompEditor(it) }
})

@Alternative
@Expandable(AltExpanderDelegator::class, subtypeOf = Alt::class)
@EditableList(classLocation = "hextant.sample.editor.AltEditorList")
sealed class Alt {
    @Token(subtypeOf = Alt::class)
    data class TestToken(val str: String): Alt() {
        companion object : TokenType<TestToken> {
            override fun compile(token: String): CompileResult<TestToken> = ok(TestToken(token))
        }
    }

    @Compound(subtypeOf = Alt::class)
    data class Comp(val x: Alt, val y: List<Alt>) : Alt()
}