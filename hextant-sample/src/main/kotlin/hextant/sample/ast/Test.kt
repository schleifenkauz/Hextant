/**
 * @author Nikolaus Knop
 */

package hextant.sample.ast

import hextant.*
import hextant.base.AbstractEditor
import hextant.codegen.*
import hextant.core.TokenType
import hextant.core.editor.TokenEditor
import hextant.core.view.TokenEditorView
import hextant.sample.ast.Alt.O
import hextant.sample.ast.Alt.TestToken
import hextant.sample.ast.editor.*
import reaktive.value.reactiveValue

object AltExpanderDelegator : ExpanderConfigurator<AltEditor<Alt>>({
    registerConstant("token") { TestTokenEditor(it) }
    registerConstant("comp") { CompEditor(it) }
})

class OtherTokenEditor(context: Context, t: TestToken = TestToken("Hello World")) :
    TokenEditor<TestToken, TokenEditorView>(context) {
    override fun compile(token: String): CompileResult<TestToken> = ok(TestToken("Hello World"))
}

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
    data class Comp(
        val x: Alt,
        val y: List<Alt>,
        @UseEditor(OtherTokenEditor::class) val z: TestToken,
        val o: O
    ) : Alt()

    @UseEditor(CustomEditor::class)
    object O : Alt()
}

class CustomEditor(context: Context, o: Alt.O = Alt.O) : AbstractEditor<Alt.O, EditorView>(context) {
    override val result: EditorResult<O> = reactiveValue(ok(O))
}