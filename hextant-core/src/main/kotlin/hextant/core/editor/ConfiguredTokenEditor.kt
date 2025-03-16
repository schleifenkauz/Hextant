package hextant.core.editor

import hextant.core.view.TokenEditorView

open class ConfiguredTokenEditor<R>(private val tokenType: TokenType<R>) : TokenEditor<R, TokenEditorView>() {
    override fun compile(token: String): R = tokenType.compile(token)
}