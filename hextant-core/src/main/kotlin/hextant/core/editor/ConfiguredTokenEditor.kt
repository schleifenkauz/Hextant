package hextant.core.editor

import hextant.context.Context
import hextant.core.view.TokenEditorView

open class ConfiguredTokenEditor<R>(private val tokenType: TokenType<R>, context: Context, text: String = "") :
    TokenEditor<R, TokenEditorView>(context, text) {
    override fun compile(token: String): R = tokenType.compile(token)
}