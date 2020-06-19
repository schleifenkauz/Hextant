/**
 * @author Nikolaus Knop
 */

package hextant.sample.ast

import hextant.codegen.*
import hextant.context.Context
import hextant.core.EditorView
import hextant.core.editor.*
import hextant.core.view.TokenEditorView
import hextant.sample.ast.Alt.O
import hextant.sample.ast.Alt.TestToken
import hextant.sample.ast.editor.*
import reaktive.value.reactiveValue
import validated.Validated
import validated.reaktive.ReactiveValidated
import validated.valid

object AltExpanderDelegator : ExpanderConfigurator<AltEditor<Alt>>({
    registerConstant("token") { TestTokenEditor(it) }
    registerConstant("comp") { CompEditor(it) }
})

class OtherTokenEditor(context: Context, t: TestToken = TestToken("Hello World")) :
    TokenEditor<TestToken, TokenEditorView>(context, t.str) {
    override fun compile(token: String): Validated<TestToken> =
        valid(TestToken("Hello World"))
}

@Alternative
@Expandable(AltExpanderDelegator::class, subtypeOf = Alt::class)
@EditableList(classLocation = "hextant.sample.editor.AltEditorList")
sealed class Alt {
    @Token(subtypeOf = Alt::class)
    data class TestToken(val str: String) : Alt() {
        companion object : TokenType<TestToken> {
            override fun compile(token: String): Validated<TestToken> =
                valid(TestToken(token))
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

class CustomEditor(context: Context, @Suppress("UNUSED_PARAMETER") o: O = O) : AbstractEditor<O, EditorView>(context) {
    override val result: ReactiveValidated<O> = reactiveValue(
        valid(
            O
        )
    )
}